package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecordingEntity
import com.example.ui.components.SharedListeningBar
import com.example.ui.components.SharedListeningRoomDialog
import com.example.ui.components.box.AestheticDecorationBox
import com.example.ui.components.box.BoxImageExporter
import com.example.ui.theme.CelestialPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SoftPink
import com.example.viewmodel.AestheticallyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackDetailScreen(
    recording: RecordingEntity,
    viewModel: AestheticallyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val currentPosMs by viewModel.audioPlayer.currentPositionMs.collectAsState()
    val totalDurationMs by viewModel.audioPlayer.totalDurationMs.collectAsState()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsState()
    val isLooping by viewModel.audioPlayer.isLooping.collectAsState()
    val showListenRoom by viewModel.showListenRoom.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }

    val effectiveTotalDuration = remember(totalDurationMs, recording.durationMs) {
        if (totalDurationMs > 1000L) totalDurationMs else recording.durationMs
    }

    val playbackProgress = remember(currentPosMs, effectiveTotalDuration) {
        if (effectiveTotalDuration > 0) (currentPosMs.toFloat() / effectiveTotalDuration.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val currentPosFormatted = remember(currentPosMs) {
        val totalSec = currentPosMs / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    val totalDurationFormatted = remember(effectiveTotalDuration) {
        val totalSec = effectiveTotalDuration / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    val dateFormatted = remember(recording.createdAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(recording.createdAt))
    }

    val waveformPoints = remember(recording.waveformPoints) {
        recording.waveformPoints.split(",").mapNotNull { it.trim().toFloatOrNull() }
    }

    // Auto-start playback on initial screen entry
    LaunchedEffect(recording.id) {
        viewModel.audioPlayer.play(recording.audioFilePath, recording.durationMs)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.audioPlayer.pause()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleFavorite(recording) }) {
                        Icon(
                            imageVector = if (recording.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (recording.isFavorite) SoftPink else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { BoxImageExporter.generateAndShareBoxImage(context, recording) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large 3D Animated Decoration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                AestheticDecorationBox(
                    modifier = Modifier.fillMaxSize(),
                    primaryColorHex = recording.primaryColorHex,
                    secondaryColorHex = recording.secondaryColorHex,
                    frameShape = recording.frameShape,
                    charmType = recording.charmType,
                    animationType = recording.animationType,
                    isPlaying = isPlaying,
                    amplitude = if (isPlaying) 0.7f else 0.2f,
                    playbackProgress = playbackProgress,
                    waveformPoints = waveformPoints,
                    isLocked = recording.isLocked,
                    showDurationBadge = totalDurationFormatted,
                    boxElevation = 16.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Locked Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recording.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showRenameDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (recording.isLocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldAccent.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Locked",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Timestamp & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatted,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = recording.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Note Card (if present)
            if (recording.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "💌 \"${recording.note}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waveform Scrubber / Slider
            Slider(
                value = playbackProgress,
                onValueChange = { newProg ->
                    val newMs = (newProg * effectiveTotalDuration).toLong()
                    viewModel.audioPlayer.seekTo(newMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Current Time / Total Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = currentPosFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Text(text = totalDurationFormatted, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Controls Row (15s Back, Prev, Center Play/Pause, Next, 15s Fwd)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 15s Back
                IconButton(onClick = { viewModel.audioPlayer.skip15Backward() }) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 15s",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Prev / Restart
                IconButton(onClick = { viewModel.audioPlayer.seekTo(0L) }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Restart",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Central Gradient Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(10.dp, CircleShape, spotColor = CelestialPrimary)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SoftPink, CelestialPrimary)
                            )
                        )
                        .clickable {
                            viewModel.audioPlayer.playOrPause(recording.audioFilePath, recording.durationMs)
                        }
                        .testTag("detail_play_pause_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next / Loop Toggle
                IconButton(onClick = { viewModel.audioPlayer.toggleLoop() }) {
                    Icon(
                        imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // 15s Forward
                IconButton(onClick = { viewModel.audioPlayer.skip15Forward() }) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 15s",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speed Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                for (spd in speeds) {
                    val isSelected = playbackSpeed == spd
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.audioPlayer.setSpeed(spd) },
                        label = { Text("${spd}x", fontSize = 11.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Row: Share Card (PNG), Lock, Rename, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Share
                DetailActionButton(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = { BoxImageExporter.generateAndShareBoxImage(context, recording) }
                )

                // Lock / Unlock
                DetailActionButton(
                    icon = if (recording.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    label = if (recording.isLocked) "Unlock" else "Lock",
                    onClick = {
                        viewModel.toggleLock(recording, !recording.isLocked, if (!recording.isLocked) "1234" else "")
                    }
                )

                // Rename
                DetailActionButton(
                    icon = Icons.Default.Edit,
                    label = "Rename",
                    onClick = { showRenameDialog = true }
                )

                // Delete
                DetailActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = "Delete",
                    isDestructive = true,
                    onClick = {
                        viewModel.deleteRecording(recording)
                        onBack()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Real-time Shared Listening Bar
            SharedListeningBar(
                partnerName = "Aaries",
                isOnline = true,
                isPlaying = isPlaying,
                onOpenRoom = { viewModel.openListenRoom() }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        RenameRecordingDialog(
            recording = recording,
            onDismiss = { showRenameDialog = false },
            onSave = { title, cat, note ->
                viewModel.renameRecording(recording, title, cat, note)
                showRenameDialog = false
            }
        )
    }

    // Shared Listening Room BottomSheet
    if (showListenRoom) {
        SharedListeningRoomDialog(
            roomCode = "AST-${(recording.id.hashCode() % 9000 + 1000)}",
            recordingTitle = recording.title,
            isPlaying = isPlaying,
            onDismiss = { viewModel.closeListenRoom() }
        )
    }
}

@Composable
private fun DetailActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
