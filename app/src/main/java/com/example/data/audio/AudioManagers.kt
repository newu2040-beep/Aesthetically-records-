package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private var isPaused = false
    private var startTimeMs = 0L
    private var accumulatedDurationMs = 0L
    private val amplitudeHistory = mutableListOf<Float>()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPausedState = MutableStateFlow(false)
    val isPausedState: StateFlow<Boolean> = _isPausedState.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    fun startRecording(fileName: String = "voice_${System.currentTimeMillis()}.m4a"): File? {
        try {
            val audioDir = File(context.filesDir, "recordings").apply { if (!exists()) mkdirs() }
            val file = File(audioDir, fileName)
            currentOutputFile = file
            amplitudeHistory.clear()

            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recorder = mr
            _isRecording.value = true
            _isPausedState.value = false
            isPaused = false
            startTimeMs = System.currentTimeMillis()
            accumulatedDurationMs = 0L

            startAmplitudePolling()
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            _isRecording.value = false
            return null
        }
    }

    private fun startAmplitudePolling() {
        recordingJob?.cancel()
        recordingJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && _isRecording.value) {
                if (!isPaused) {
                    val maxAmp = try {
                        recorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val normalized = (maxAmp / 32767f).coerceIn(0.05f, 1f)
                    _currentAmplitude.value = normalized
                    amplitudeHistory.add(normalized)

                    val elapsed = System.currentTimeMillis() - startTimeMs + accumulatedDurationMs
                    _durationMs.value = elapsed
                }
                delay(60)
            }
        }
    }

    fun pauseRecording() {
        if (_isRecording.value && !isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.pause()
                accumulatedDurationMs += System.currentTimeMillis() - startTimeMs
                isPaused = true
                _isPausedState.value = true
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Pause error", e)
            }
        }
    }

    fun resumeRecording() {
        if (_isRecording.value && isPaused && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.resume()
                startTimeMs = System.currentTimeMillis()
                isPaused = false
                _isPausedState.value = false
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Resume error", e)
            }
        }
    }

    fun stopRecording(): Pair<File?, List<Float>> {
        var recordedFile: File? = null
        try {
            recordingJob?.cancel()
            recorder?.apply {
                stop()
                release()
            }
            recordedFile = currentOutputFile
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop error", e)
        } finally {
            recorder = null
            _isRecording.value = false
            _isPausedState.value = false
            _currentAmplitude.value = 0f
        }

        // Simplify amplitude history to ~24 bars
        val samplePoints = if (amplitudeHistory.size <= 24) {
            if (amplitudeHistory.isEmpty()) listOf(0.4f, 0.7f, 0.9f, 0.5f, 0.8f, 0.6f, 0.3f, 0.7f) else amplitudeHistory.toList()
        } else {
            val step = amplitudeHistory.size.toFloat() / 24f
            (0 until 24).map { i ->
                val index = (i * step).toInt().coerceIn(0, amplitudeHistory.size - 1)
                amplitudeHistory[index]
            }
        }

        return Pair(recordedFile, samplePoints)
    }

    fun cancelRecording() {
        try {
            recordingJob?.cancel()
            recorder?.apply {
                stop()
                release()
            }
            currentOutputFile?.delete()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Cancel error", e)
        } finally {
            recorder = null
            _isRecording.value = false
            _isPausedState.value = false
        }
    }
}

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var synthTrack: AudioTrack? = null
    private var isSyntheticPlaying = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(1L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    fun playOrPause(filePath: String, totalDurationFallback: Long = 60000L) {
        if (_isPlaying.value) {
            pause()
        } else {
            play(filePath, totalDurationFallback)
        }
    }

    fun play(filePath: String, totalDurationFallback: Long = 60000L) {
        stop()
        val file = File(filePath)
        if (file.exists() && file.length() > 0) {
            playRealFile(file)
        } else {
            // Play procedural calming aesthetic music box melody for sample voice notes
            playSyntheticMelody(totalDurationFallback)
        }
    }

    private fun playRealFile(file: File) {
        try {
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { player ->
                    _totalDurationMs.value = player.duration.toLong().coerceAtLeast(1000L)
                    player.isLooping = _isLooping.value
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        player.playbackParams = player.playbackParams.setSpeed(_playbackSpeed.value)
                    }
                    player.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
                setOnCompletionListener {
                    if (!_isLooping.value) {
                        _isPlaying.value = false
                        _currentPositionMs.value = _totalDurationMs.value
                    }
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed real playback, falling back to synth", e)
            playSyntheticMelody(60000L)
        }
    }

    private fun playSyntheticMelody(durationMs: Long) {
        _totalDurationMs.value = durationMs
        _isPlaying.value = true
        isSyntheticPlaying = true

        startProgressTracker()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 22050
                val notes = listOf(523.25, 659.25, 783.99, 1046.50, 880.0, 783.99, 659.25, 587.33) // C5, E5, G5, C6, A5, G5, E5, D5
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                synthTrack = track
                track.play()

                var noteIndex = 0
                while (isActive && isSyntheticPlaying && _isPlaying.value) {
                    val freq = notes[noteIndex % notes.size]
                    noteIndex++
                    val noteDurationMs = (400 / _playbackSpeed.value).toInt()
                    val numSamples = (sampleRate * (noteDurationMs / 1000f)).toInt()
                    val samples = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = (1.0 - (i.toDouble() / numSamples)) // Decay like a music box chime
                        val sine = (sin(2.0 * Math.PI * freq * t) * 0.4 + sin(4.0 * Math.PI * freq * t) * 0.1) * envelope
                        samples[i] = (sine * 16000).toInt().coerceIn(-32768, 32767).toShort()
                    }
                    track.write(samples, 0, numSamples)
                    delay(20)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Synth error", e)
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && _isPlaying.value) {
                if (mediaPlayer != null) {
                    try {
                        _currentPositionMs.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    } catch (e: Exception) {
                        // ignore
                    }
                } else if (isSyntheticPlaying) {
                    val next = _currentPositionMs.value + (100 * _playbackSpeed.value).toLong()
                    if (next >= _totalDurationMs.value) {
                        if (_isLooping.value) {
                            _currentPositionMs.value = 0L
                        } else {
                            _currentPositionMs.value = _totalDurationMs.value
                            pause()
                        }
                    } else {
                        _currentPositionMs.value = next
                    }
                }
                delay(100)
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            // ignore
        }
        isSyntheticPlaying = false
        synthTrack?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun resume() {
        if (!_isPlaying.value) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                    startProgressTracker()
                } catch (e: Exception) {
                    playSyntheticMelody(_totalDurationMs.value)
                }
            } else {
                playSyntheticMelody(_totalDurationMs.value)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _totalDurationMs.value)
        _currentPositionMs.value = clamped
        try {
            mediaPlayer?.seekTo(clamped.toInt())
        } catch (e: Exception) {
            // ignore
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
            try {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun toggleLoop() {
        val newLoop = !_isLooping.value
        _isLooping.value = newLoop
        mediaPlayer?.isLooping = newLoop
    }

    fun skip15Forward() {
        seekTo(_currentPositionMs.value + 15000L)
    }

    fun skip15Backward() {
        seekTo(_currentPositionMs.value - 15000L)
    }

    fun stop() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        try {
            synthTrack?.stop()
            synthTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        synthTrack = null
        isSyntheticPlaying = false
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }
}
