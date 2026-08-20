package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.components.AestheticallyBottomNav
import com.example.ui.components.NavDestination
import com.example.ui.components.PinLockDialog
import com.example.ui.screens.*
import com.example.ui.theme.AestheticallyTheme
import com.example.viewmodel.AestheticallyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AestheticallyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isOnboardingDone by viewModel.isOnboardingDone.collectAsState()
            val currentDetailRecording by viewModel.currentDetailRecording.collectAsState()
            val showPinDialog by viewModel.showPinDialog.collectAsState()
            val pendingUnlockRecording by viewModel.pendingUnlockRecording.collectAsState()

            var currentNav by remember { mutableStateOf(NavDestination.HOME) }

            // Automatic runtime permission request on start
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                // Permissions updated
            }

            LaunchedEffect(Unit) {
                val permsToRequest = mutableListOf<String>()
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    permsToRequest.add(Manifest.permission.RECORD_AUDIO)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        permsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                }
                if (permsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permsToRequest.toTypedArray())
                }
            }

            AestheticallyTheme(
                preset = currentTheme,
                darkTheme = isDarkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnboardingDone) {
                        OnboardingScreen(
                            onGetStarted = {
                                viewModel.setOnboardingDone()
                                currentNav = NavDestination.RECORD
                            }
                        )
                    } else if (currentDetailRecording != null) {
                        // Playback / Box Detail Screen
                        PlaybackDetailScreen(
                            recording = currentDetailRecording!!,
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearDetailRecording()
                            }
                        )
                    } else {
                        // Main Bottom Nav Host
                        Scaffold(
                            bottomBar = {
                                AestheticallyBottomNav(
                                    currentDestination = currentNav,
                                    onNavigate = { currentNav = it }
                                )
                            },
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            containerColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentNav) {
                                    NavDestination.HOME -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToRecord = { currentNav = NavDestination.RECORD },
                                            onNavigateToDetail = { rec ->
                                                viewModel.selectDetailRecording(rec)
                                            }
                                        )
                                    }
                                    NavDestination.TEMPLATES -> {
                                        TemplatesScreen(
                                            viewModel = viewModel,
                                            onUseTemplate = {
                                                currentNav = NavDestination.RECORD
                                            }
                                        )
                                    }
                                    NavDestination.RECORD -> {
                                        RecordScreen(
                                            viewModel = viewModel,
                                            onNavigateToTemplates = { currentNav = NavDestination.TEMPLATES },
                                            onFinishAndOpenDetail = {
                                                // After save, the ViewModel sets currentDetailRecording
                                            }
                                        )
                                    }
                                    NavDestination.STUDIO -> {
                                        StudioScreen(
                                            viewModel = viewModel,
                                            onTemplateSaved = {
                                                currentNav = NavDestination.TEMPLATES
                                            }
                                        )
                                    }
                                    NavDestination.SETTINGS -> {
                                        SettingsScreen(
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Global PIN unlock dialog
                    if (showPinDialog && pendingUnlockRecording != null) {
                        PinLockDialog(
                            title = "Secret Voice Box 🔒",
                            subtitle = "Enter PIN to open \"${pendingUnlockRecording?.title}\"",
                            expectedPin = pendingUnlockRecording?.pinCode?.ifBlank { "1234" } ?: "1234",
                            onSuccess = { viewModel.onPinSuccess() },
                            onDismiss = { viewModel.dismissPinDialog() }
                        )
                    }
                }
            }
        }
    }
}
