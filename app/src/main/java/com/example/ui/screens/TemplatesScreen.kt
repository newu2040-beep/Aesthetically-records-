package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoxAnimationType
import com.example.data.model.TemplateEntity
import com.example.ui.components.box.AestheticDecorationBox
import com.example.ui.theme.CelestialPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SoftPink
import com.example.viewmodel.AestheticallyViewModel

@Composable
fun TemplatesScreen(
    viewModel: AestheticallyViewModel,
    onUseTemplate: (TemplateEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    var selectedTemplate by remember { mutableStateOf(templates.firstOrNull() ?: com.example.data.db.AppDatabase.PREMADE_TEMPLATES[0]) }
    var selectedAnimation by remember { mutableStateOf(selectedTemplate.animationType) }
    var isPreviewPlaying by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    LaunchedEffect(selectedTemplate) {
        selectedAnimation = selectedTemplate.animationType
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Template Gallery ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isPreviewPlaying = !isPreviewPlaying }) {
                        Icon(
                            imageVector = if (isPreviewPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Preview Audio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp) // accommodate floating bottom nav
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            val updated = selectedTemplate.copy(animationType = selectedAnimation)
                            viewModel.setSelectedRecordingTemplate(updated)
                            onUseTemplate(updated)
                        },
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
                            .testTag("use_template_btn")
                    ) {
                        Text(
                            text = "Use This Template ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // Horizontal Template Selector Carousel
            Text(
                text = "Choose Theme Family",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(templates, key = { it.id }) { template ->
                    val isSelected = template.id == selectedTemplate.id
                    val primaryC = try {
                        Color(android.graphics.Color.parseColor(template.primaryColorHex))
                    } catch (e: Exception) {
                        CelestialPrimary
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryC.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, primaryC) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { selectedTemplate = template }
                            .testTag("template_item_${template.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(primaryC),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (template.charmType) {
                                        "bunny" -> "🐰🌸"
                                        "kitty" -> "🐱🌿"
                                        "teddy" -> "🧸☕"
                                        "moon" -> "🌙✨"
                                        "ribbon" -> "🎀💖"
                                        else -> "⭐✨"
                                    },
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = template.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Big Live Interactive Box Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AestheticDecorationBox(
                    modifier = Modifier.fillMaxSize(),
                    primaryColorHex = selectedTemplate.primaryColorHex,
                    secondaryColorHex = selectedTemplate.secondaryColorHex,
                    frameShape = selectedTemplate.frameShape,
                    charmType = selectedTemplate.charmType,
                    animationType = selectedAnimation,
                    isPlaying = isPreviewPlaying,
                    amplitude = if (isPreviewPlaying) 0.65f else 0.2f,
                    showDurationBadge = "Preview",
                    boxElevation = 14.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Template Header: Title, Premium Badge, Author, Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTemplate.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedTemplate.isPremium) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftPink.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Premium ✨",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "by ${selectedTemplate.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedTemplate.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animation Style Switcher
            Text(
                text = "Animation Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val animTypes = listOf(
                    BoxAnimationType.PULSE,
                    BoxAnimationType.FLOAT,
                    BoxAnimationType.PETALS,
                    BoxAnimationType.WAVE,
                    BoxAnimationType.TWINKLE
                )

                for (anim in animTypes) {
                    val isSelected = selectedAnimation == anim.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAnimation = anim.id },
                        label = { Text(anim.displayName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Included Elements Checklist
            Text(
                text = "Includes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            val includesList = listOf(
                Pair("Background", Icons.Default.Palette),
                Pair("Frame", Icons.Default.CropPortrait),
                Pair("Stickers", Icons.Default.SentimentSatisfiedAlt),
                Pair("Waveform", Icons.Default.GraphicEq),
                Pair("Particles", Icons.Default.Grain)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for ((name, icon) in includesList) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
