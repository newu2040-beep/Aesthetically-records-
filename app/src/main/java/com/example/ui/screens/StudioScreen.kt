package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
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
import com.example.data.model.BoxAnimationType
import com.example.data.model.CharmType
import com.example.data.model.FrameShape
import com.example.ui.components.box.AestheticDecorationBox
import com.example.ui.theme.CelestialPrimary
import com.example.ui.theme.SoftPink
import com.example.viewmodel.AestheticallyViewModel

@Composable
fun StudioScreen(
    viewModel: AestheticallyViewModel,
    onTemplateSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studioName by viewModel.studioTemplateName.collectAsState()
    val primaryColorHex by viewModel.studioPrimaryColorHex.collectAsState()
    val secondaryColorHex by viewModel.studioSecondaryColorHex.collectAsState()
    val frameShape by viewModel.studioFrameShape.collectAsState()
    val charmType by viewModel.studioCharmType.collectAsState()
    val animationType by viewModel.studioAnimationType.collectAsState()

    val paletteOptions = listOf(
        Pair("Lavender", Pair("#B39DDB", "#EDE7F6")),
        Pair("Sakura", Pair("#F48FB1", "#FFF0F4")),
        Pair("Matcha", Pair("#A5D6A7", "#E8F5E9")),
        Pair("Latte", Pair("#D7CCC8", "#EFEBE9")),
        Pair("Starlight", Pair("#90CAF9", "#E3F2FD")),
        Pair("Cyber Pop", Pair("#FF80AB", "#FCE4EC")),
        Pair("Sunset", Pair("#FFAB91", "#FBE9E7")),
        Pair("Mint", Pair("#80CBC4", "#E0F2F1"))
    )

    val frameShapes = FrameShape.values()
    val charmTypes = CharmType.values()
    val animationTypes = BoxAnimationType.values()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Custom Studio ✨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
                    .padding(bottom = 80.dp) // space for bottom nav
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Button(
                        onClick = {
                            viewModel.saveCustomStudioTemplate()
                            Toast.makeText(context, "Saved template \"$studioName\" to library! ✨", Toast.LENGTH_SHORT).show()
                            onTemplateSaved()
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
                            .testTag("save_studio_template_btn")
                    ) {
                        Text(
                            text = "Save to My Templates ✨",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Live Box Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AestheticDecorationBox(
                    modifier = Modifier.fillMaxSize(),
                    primaryColorHex = primaryColorHex,
                    secondaryColorHex = secondaryColorHex,
                    frameShape = frameShape,
                    charmType = charmType,
                    animationType = animationType,
                    isPlaying = true,
                    amplitude = 0.6f,
                    showDurationBadge = "Studio",
                    boxElevation = 12.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Template Name Input
            OutlinedTextField(
                value = studioName,
                onValueChange = { viewModel.updateStudioConfig(name = it) },
                label = { Text("Template Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("studio_name_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Color Palette Selector
            Text(
                text = "1. Base Palette",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(paletteOptions) { (name, hexPair) ->
                    val isSelected = primaryColorHex == hexPair.first
                    val col = try {
                        Color(android.graphics.Color.parseColor(hexPair.first))
                    } catch (e: Exception) {
                        CelestialPrimary
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.updateStudioConfig(
                                    primaryHex = hexPair.first,
                                    secondaryHex = hexPair.second
                                )
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(col),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Window Frame Shape Selector
            Text(
                text = "2. Cutout Frame Shape",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frameShapes) { shape ->
                    val isSelected = frameShape == shape.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateStudioConfig(shape = shape.id) },
                        label = { Text(shape.displayName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Charm & Sticker Selector
            Text(
                text = "3. Dangling Charm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(charmTypes) { charm ->
                    val isSelected = charmType == charm.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .clickable { viewModel.updateStudioConfig(charm = charm.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = charm.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = charm.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Animation Style Selector
            Text(
                text = "4. Ambient Particle Effect",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(animationTypes) { anim ->
                    val isSelected = animationType == anim.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateStudioConfig(anim = anim.id) },
                        label = { Text(anim.displayName, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
