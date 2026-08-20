package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.AudioPlayerManager
import com.example.data.audio.AudioRecorderManager
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.RecordingsRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AestheticallyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val recordingsRepository = RecordingsRepository(database)
    val settingsRepository = SettingsRepository(application)

    val audioRecorder = AudioRecorderManager(application)
    val audioPlayer = AudioPlayerManager(application)

    // Filter & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Template Selection for Recording
    private val _selectedRecordingTemplate = MutableStateFlow<TemplateEntity>(AppDatabase.PREMADE_TEMPLATES[0])
    val selectedRecordingTemplate: StateFlow<TemplateEntity> = _selectedRecordingTemplate.asStateFlow()

    // Studio Custom Template Builder State
    private val _studioPrimaryColorHex = MutableStateFlow("#F48FB1")
    val studioPrimaryColorHex: StateFlow<String> = _studioPrimaryColorHex.asStateFlow()

    private val _studioSecondaryColorHex = MutableStateFlow("#FFF0F4")
    val studioSecondaryColorHex: StateFlow<String> = _studioSecondaryColorHex.asStateFlow()

    private val _studioFrameShape = MutableStateFlow(FrameShape.PETAL.id)
    val studioFrameShape: StateFlow<String> = _studioFrameShape.asStateFlow()

    private val _studioCharmType = MutableStateFlow(CharmType.BUNNY.id)
    val studioCharmType: StateFlow<String> = _studioCharmType.asStateFlow()

    private val _studioAnimationType = MutableStateFlow(BoxAnimationType.PETALS.id)
    val studioAnimationType: StateFlow<String> = _studioAnimationType.asStateFlow()

    private val _studioTemplateName = MutableStateFlow("My Sweet Blossom")
    val studioTemplateName: StateFlow<String> = _studioTemplateName.asStateFlow()

    // Active Recording Selected for Playback Detail
    private val _currentDetailRecording = MutableStateFlow<RecordingEntity?>(null)
    val currentDetailRecording: StateFlow<RecordingEntity?> = _currentDetailRecording.asStateFlow()

    // Security & Unlock State
    private val _pendingUnlockRecording = MutableStateFlow<RecordingEntity?>(null)
    val pendingUnlockRecording: StateFlow<RecordingEntity?> = _pendingUnlockRecording.asStateFlow()

    // UI Dialogs
    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _showListenRoom = MutableStateFlow(false)
    val showListenRoom: StateFlow<Boolean> = _showListenRoom.asStateFlow()

    // Recordings Filtered List
    val recordings: StateFlow<List<RecordingEntity>> = combine(
        recordingsRepository.allRecordings,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { rec ->
            val matchesQuery = query.isBlank() || rec.title.contains(query, ignoreCase = true) || rec.note.contains(query, ignoreCase = true)
            val matchesCategory = when (category) {
                "All" -> true
                "Favorites" -> rec.isFavorite
                "Locked" -> rec.isLocked
                "Recent" -> true
                else -> rec.category.equals(category, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<TemplateEntity>> = recordingsRepository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppDatabase.PREMADE_TEMPLATES)

    val currentTheme: StateFlow<AppThemePreset> = settingsRepository.selectedTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemePreset.CELESTIAL)

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isOnboardingDone: StateFlow<Boolean> = settingsRepository.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedRecordingTemplate(template: TemplateEntity) {
        _selectedRecordingTemplate.value = template
    }

    fun selectDetailRecording(recording: RecordingEntity?) {
        _currentDetailRecording.value = recording
    }

    fun clearDetailRecording() {
        _currentDetailRecording.value = null
    }

    fun toggleFavorite(recording: RecordingEntity) {
        viewModelScope.launch {
            recordingsRepository.toggleFavorite(recording)
            if (_currentDetailRecording.value?.id == recording.id) {
                _currentDetailRecording.value = recording.copy(isFavorite = !recording.isFavorite)
            }
        }
    }

    fun toggleLock(recording: RecordingEntity, isLocked: Boolean, pin: String) {
        viewModelScope.launch {
            recordingsRepository.updateLock(recording, isLocked, pin)
            if (_currentDetailRecording.value?.id == recording.id) {
                _currentDetailRecording.value = recording.copy(isLocked = isLocked, pinCode = pin)
            }
        }
    }

    fun renameRecording(recording: RecordingEntity, newTitle: String, category: String, note: String) {
        viewModelScope.launch {
            recordingsRepository.renameRecording(recording, newTitle, category, note)
            if (_currentDetailRecording.value?.id == recording.id) {
                _currentDetailRecording.value = recording.copy(title = newTitle, category = category, note = note)
            }
        }
    }

    fun deleteRecording(recording: RecordingEntity) {
        viewModelScope.launch {
            audioPlayer.stop()
            recordingsRepository.deleteRecording(recording)
            if (_currentDetailRecording.value?.id == recording.id) {
                _currentDetailRecording.value = null
            }
        }
    }

    // Studio Builder setters
    fun updateStudioConfig(
        name: String = _studioTemplateName.value,
        primaryHex: String = _studioPrimaryColorHex.value,
        secondaryHex: String = _studioSecondaryColorHex.value,
        shape: String = _studioFrameShape.value,
        charm: String = _studioCharmType.value,
        anim: String = _studioAnimationType.value
    ) {
        _studioTemplateName.value = name
        _studioPrimaryColorHex.value = primaryHex
        _studioSecondaryColorHex.value = secondaryHex
        _studioFrameShape.value = shape
        _studioCharmType.value = charm
        _studioAnimationType.value = anim
    }

    fun saveCustomStudioTemplate() {
        viewModelScope.launch {
            val customTemplate = TemplateEntity(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = _studioTemplateName.value.ifBlank { "My Aesthetic Box" },
                author = "You",
                description = "Custom-designed aesthetic voice box with personalized frame and charms.",
                primaryColorHex = _studioPrimaryColorHex.value,
                secondaryColorHex = _studioSecondaryColorHex.value,
                frameShape = _studioFrameShape.value,
                charmType = _studioCharmType.value,
                animationType = _studioAnimationType.value,
                isCustom = true,
                isPremium = false
            )
            recordingsRepository.saveCustomTemplate(customTemplate)
            _selectedRecordingTemplate.value = customTemplate
        }
    }

    // Recording operations
    fun startRecording() {
        audioPlayer.stop()
        audioRecorder.startRecording()
    }

    fun pauseRecording() = audioRecorder.pauseRecording()

    fun resumeRecording() = audioRecorder.resumeRecording()

    fun finishRecording(title: String, category: String, note: String, isLocked: Boolean = false, pin: String = "") {
        val (file, amplitudes) = audioRecorder.stopRecording()
        val template = _selectedRecordingTemplate.value
        val pointsStr = amplitudes.joinToString(",") { String.format(Locale.US, "%.2f", it) }
        val durationMs = audioRecorder.durationMs.value.coerceAtLeast(1000L)

        val newRec = RecordingEntity(
            title = title.ifBlank { "Voice Memo #${System.currentTimeMillis() % 1000}" },
            durationMs = durationMs,
            audioFilePath = file?.absolutePath ?: "voice_${System.currentTimeMillis()}.m4a",
            templateId = template.id,
            primaryColorHex = template.primaryColorHex,
            secondaryColorHex = template.secondaryColorHex,
            frameShape = template.frameShape,
            charmType = template.charmType,
            animationType = template.animationType,
            isLocked = isLocked,
            pinCode = pin,
            category = category,
            note = note,
            waveformPoints = pointsStr
        )

        viewModelScope.launch {
            recordingsRepository.insertRecording(newRec)
            _currentDetailRecording.value = newRec
        }
    }

    fun cancelRecording() {
        audioRecorder.cancelRecording()
    }

    // Settings actions
    fun setTheme(preset: AppThemePreset) {
        viewModelScope.launch {
            settingsRepository.setTheme(preset)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun setOnboardingDone() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
        }
    }

    fun promptUnlockRecording(recording: RecordingEntity) {
        _pendingUnlockRecording.value = recording
        _showPinDialog.value = true
    }

    fun onPinSuccess() {
        _pendingUnlockRecording.value?.let { rec ->
            _currentDetailRecording.value = rec
        }
        _showPinDialog.value = false
        _pendingUnlockRecording.value = null
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
        _pendingUnlockRecording.value = null
    }

    fun openListenRoom() {
        _showListenRoom.value = true
    }

    fun closeListenRoom() {
        _showListenRoom.value = false
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        audioRecorder.cancelRecording()
    }
}
