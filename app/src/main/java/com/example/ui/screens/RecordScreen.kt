package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.service.RecordingService
import com.example.ui.components.box.AestheticDecorationBox
import com.example.ui.theme.CelestialPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SoftPink
import com.example.viewmodel.AestheticallyViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: AestheticallyViewModel,
    onNavigateToTemplates: () -> Unit,
    onFinishAndOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRecording by viewModel.audioRecorder.isRecording.collectAsState()
    val isPaused by viewModel.audioRecorder.isPausedState.collectAsState()
    val currentAmplitude by viewModel.audioRecorder.currentAmplitude.collectAsState()
    val durationMs by viewModel.audioRecorder.durationMs.collectAsState()
    val selectedTemplate by viewModel.selectedRecordingTemplate.collectAsState()

    var recordTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Daily") }
    var noteText by remember { mutableStateOf("") }
    var isLocked by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("1234") }
    var hasFinishedRecording by remember { mutableStateOf(false) }

    val categories = listOf("Daily", "Letters", "Thoughts", "Dreams", "Reminders")

    // Permission launcher for Microphone and Notification
    var hasMicPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasMicPermission = perms[Manifest.permission.RECORD_AUDIO] ?: false
        if (hasMicPermission) {
            viewModel.startRecording()
            startRecordingService(context)
        }
    }

    val formattedDuration = remember(durationMs) {
        val sec = (durationMs / 1000) % 60
        val min = (durationMs / 1000) / 60
        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording && !isPaused) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    fun handleRecordClick() {
        if (!isRecording) {
            hasFinishedRecording = false
            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        } else if (isPaused) {
            viewModel.resumeRecording()
        } else {
            viewModel.pauseRecording()
        }
    }

    fun handleStopAndFinish() {
        stopRecordingService(context)
        hasFinishedRecording = true
        if (recordTitle.isBlank()) {
            recordTitle = "Voice Letter #${(System.currentTimeMillis() % 1000)}"
        }
    }

    fun handleSaveToLibrary() {
        viewModel.finishRecording(
            title = recordTitle,
            category = selectedCategory,
            note = noteText,
            isLocked = isLocked,
            pin = pinCode
        )
        onFinishAndOpenDetail()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Studio Recording 🎙️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onNavigateToTemplates() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedTemplate.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Template",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
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
            Spacer(modifier = Modifier.height(10.dp))

            // Large Interactive Live Decoration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                AestheticDecorationBox(
                    modifier = Modifier.fillMaxSize(),
                    primaryColorHex = selectedTemplate.primaryColorHex,
                    secondaryColorHex = selectedTemplate.secondaryColorHex,
                    frameShape = selectedTemplate.frameShape,
                    charmType = selectedTemplate.charmType,
                    animationType = selectedTemplate.animationType,
                    isPlaying = isRecording && !isPaused,
                    amplitude = if (isRecording && !isPaused) currentAmplitude else 0.25f,
                    showDurationBadge = formattedDuration,
                    isLocked = isLocked,
                    boxElevation = 14.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Indicator & Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPaused) GoldAccent else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPaused) "Recording Paused ⏸️" else "Live Recording 🔴",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                } else if (hasFinishedRecording) {
                    Text(
                        text = "Recording Complete ✨ (Ready to Save)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Tap mic to start recording (Max 10 min)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedDuration,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Recording Controls
            if (!hasFinishedRecording) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Discard Button (visible during recording)
                    if (isRecording) {
                        IconButton(
                            onClick = {
                                viewModel.cancelRecording()
                                stopRecordingService(context)
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Discard Recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Main Center Mic Button
                    Box(
                        modifier = Modifier
                            .scale(micPulse)
                            .size(76.dp)
                            .shadow(12.dp, CircleShape, spotColor = CelestialPrimary)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SoftPink, CelestialPrimary)
                                )
                            )
                            .clickable { handleRecordClick() }
                            .testTag("record_screen_mic_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!isRecording) Icons.Default.Mic else if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Mic",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Finish / Stop Checkmark Button
                    if (isRecording) {
                        IconButton(
                            onClick = { handleStopAndFinish() },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Finish Recording",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Post-Recording Save Form
            AnimatedVisibility(visible = hasFinishedRecording) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = recordTitle,
                        onValueChange = { recordTitle = it },
                        label = { Text("Title your voice art") },
                        placeholder = { Text("e.g., Morning Thoughts, Little Whispers...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("record_title_input")
                    )

                    // Category Selector
                    Column {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (cat in categories) {
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Add journal note or voice letter message") },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Lock with PIN Switch
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Lock Voice Note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Requires 4-digit PIN to open", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(checked = isLocked, onCheckedChange = { isLocked = it })
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { handleSaveToLibrary() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = CelestialPrimary)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SoftPink, CelestialPrimary)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .testTag("save_recording_btn")
                    ) {
                        Text(
                            text = "Save to Art Library ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private fun startRecordingService(context: Context) {
    try {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    } catch (e: Exception) {
        // ignore
    }
}

private fun stopRecordingService(context: Context) {
    try {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP
        }
        context.startService(intent)
    } catch (e: Exception) {
        // ignore
    }
}
