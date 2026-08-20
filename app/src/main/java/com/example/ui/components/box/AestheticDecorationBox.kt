package com.example.ui.components.box

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BoxAnimationType
import com.example.data.model.CharmType
import com.example.data.model.FrameShape
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AestheticDecorationBox(
    modifier: Modifier = Modifier,
    primaryColorHex: String = "#F48FB1",
    secondaryColorHex: String = "#FFF0F4",
    frameShape: String = FrameShape.PETAL.id,
    charmType: String = CharmType.BUNNY.id,
    animationType: String = BoxAnimationType.PETALS.id,
    isPlaying: Boolean = false,
    amplitude: Float = 0.3f,
    playbackProgress: Float = 0f,
    waveformPoints: List<Float> = emptyList(),
    isLocked: Boolean = false,
    showDurationBadge: String? = null,
    boxElevation: Dp = 10.dp
) {
    val primaryColor = remember(primaryColorHex) {
        try { Color(android.graphics.Color.parseColor(primaryColorHex)) }
        catch (e: Exception) { CelestialPrimary }
    }
    val secondaryColor = remember(secondaryColorHex) {
        try { Color(android.graphics.Color.parseColor(secondaryColorHex)) }
        catch (e: Exception) { CelestialSurfaceVariant }
    }

    // Infinite animation clock for particles and gentle breathing
    val infiniteTransition = rememberInfiniteTransition(label = "box_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.03f else 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 800 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(boxElevation, RoundedCornerShape(28.dp), ambientColor = primaryColor.copy(alpha = 0.4f), spotColor = primaryColor.copy(alpha = 0.6f))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.95f),
                        primaryColor,
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.6f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 800f)
                )
            )
            .testTag("aesthetic_decoration_box")
    ) {
        // 1. Vector Box Canvas: Box Body, Stitches, Gold Corners, Hanging Keychain, Cutout Frame, and Reactive Soundwave
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // A. Draw Outer Leather Stitches
            val stitchPadding = 14.dp.toPx()
            val stitchRect = Size(canvasW - stitchPadding * 2, canvasH - stitchPadding * 2)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(stitchPadding, stitchPadding),
                size = stitchRect,
                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            // B. Draw Golden Metal Corner Brackets (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
            val cornerSize = 26.dp.toPx()
            val goldBrush = Brush.linearGradient(
                colors = listOf(GoldAccentLight, GoldAccent, Color(0xFFE5A93C)),
                start = Offset.Zero,
                end = Offset(cornerSize, cornerSize)
            )
            // Top-Left Corner
            drawCornerBracket(this, Offset(stitchPadding - 2.dp.toPx(), stitchPadding - 2.dp.toPx()), cornerSize, 0f, goldBrush)
            // Top-Right Corner
            drawCornerBracket(this, Offset(canvasW - stitchPadding + 2.dp.toPx(), stitchPadding - 2.dp.toPx()), cornerSize, 90f, goldBrush)
            // Bottom-Right Corner
            drawCornerBracket(this, Offset(canvasW - stitchPadding + 2.dp.toPx(), canvasH - stitchPadding + 2.dp.toPx()), cornerSize, 180f, goldBrush)
            // Bottom-Left Corner
            drawCornerBracket(this, Offset(stitchPadding - 2.dp.toPx(), canvasH - stitchPadding + 2.dp.toPx()), cornerSize, 270f, goldBrush)

            // C. Draw Right Side Clasp / Strap
            val strapWidth = 24.dp.toPx()
            val strapHeight = 44.dp.toPx()
            val strapX = canvasW - strapWidth
            val strapY = canvasH * 0.4f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.9f))
                ),
                topLeft = Offset(strapX, strapY),
                size = Size(strapWidth, strapHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // Golden snap rivet
            drawCircle(
                brush = goldBrush,
                radius = 6.dp.toPx(),
                center = Offset(canvasW - strapWidth * 0.45f, strapY + strapHeight / 2)
            )

            // Golden chain links hanging down
            val chainStartX = canvasW - strapWidth * 0.45f
            val chainStartY = strapY + strapHeight / 2 + 6.dp.toPx()
            for (i in 0 until 3) {
                drawCircle(
                    color = GoldAccent,
                    radius = 3.dp.toPx(),
                    center = Offset(chainStartX, chainStartY + i * 8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // D. Center Cutout Window
            val center = Offset(canvasW * 0.5f, canvasH * 0.52f)
            val windowRadius = canvasW * 0.30f

            // Frame Bevel & Gold Border
            drawFrameShape(
                drawScope = this,
                shape = frameShape,
                center = center,
                radius = windowRadius + 6.dp.toPx(),
                brush = goldBrush,
                style = Fill
            )
            // Frame Inner Window Background (Deep gradient)
            val windowBgBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2D1B4E),
                    Color(0xFF1E1035),
                    Color(0xFF120822)
                ),
                center = center,
                radius = windowRadius
            )
            drawFrameShape(
                drawScope = this,
                shape = frameShape,
                center = center,
                radius = windowRadius,
                brush = windowBgBrush,
                style = Fill
            )

            // Inner Frame Golden Outline
            drawFrameShape(
                drawScope = this,
                shape = frameShape,
                center = center,
                radius = windowRadius,
                brush = Brush.sweepGradient(
                    colors = listOf(GoldAccent, GoldAccentLight, Color(0xFFFFA07A), GoldAccent),
                    center = center
                ),
                style = Stroke(width = 3.dp.toPx())
            )

            // E. Render Soundwave / Frequency Bars inside Window
            val barsCount = 18
            val barWidth = 3.5.dp.toPx()
            val spacing = 3.dp.toPx()
            val totalWaveWidth = barsCount * (barWidth + spacing)
            val startWaveX = center.x - totalWaveWidth / 2

            val defaultPoints = listOf(
                0.25f, 0.45f, 0.75f, 0.55f, 0.90f, 0.65f, 0.85f, 0.40f,
                0.95f, 0.80f, 0.60f, 0.45f, 0.70f, 0.85f, 0.50f, 0.75f, 0.35f, 0.20f
            )
            val points = if (waveformPoints.size >= barsCount) waveformPoints else defaultPoints

            for (i in 0 until barsCount) {
                val pointVal = points.getOrElse(i) { 0.4f }
                val dynamicAmp = if (isPlaying) {
                    val sineW = sin((shimmerPhase * 2f + i * 25f) * Math.PI.toFloat() / 180f)
                    (pointVal * 0.7f + sineW * 0.3f + amplitude * 0.4f).coerceIn(0.15f, 1f)
                } else {
                    pointVal.coerceIn(0.15f, 1f)
                }

                val barH = (windowRadius * 1.1f * dynamicAmp).coerceAtLeast(6.dp.toPx())
                val x = startWaveX + i * (barWidth + spacing)
                val yTop = center.y - barH / 2
                val yBottom = center.y + barH / 2

                // Highlight bars up to playbackProgress
                val isPlayed = (i.toFloat() / barsCount) <= playbackProgress
                val barColor = if (isPlaying && isPlayed) {
                    Brush.verticalGradient(
                        colors = listOf(SoftPink, Color.White, GoldAccent),
                        startY = yTop,
                        endY = yBottom
                    )
                } else if (isPlaying) {
                    Brush.verticalGradient(
                        colors = listOf(SoftPurple, primaryColor.copy(alpha = 0.8f)),
                        startY = yTop,
                        endY = yBottom
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.9f), primaryColor.copy(alpha = 0.6f)),
                        startY = yTop,
                        endY = yBottom
                    )
                }

                drawRoundRect(
                    brush = barColor,
                    topLeft = Offset(x, yTop),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            // F. Particle Effects Layer based on animationType
            drawAnimatedParticles(
                drawScope = this,
                animationType = animationType,
                center = center,
                phase = shimmerPhase,
                isPlaying = isPlaying,
                primaryColor = primaryColor
            )
        }

        // 2. Top-Left Floating Floral/Ribbon Bow Decor
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = 10.dp)
        ) {
            when (frameShape) {
                FrameShape.PETAL.id, FrameShape.HEART.id -> {
                    Text(text = "🌸🎀", fontSize = 24.sp)
                }
                FrameShape.STAR.id -> {
                    Text(text = "⭐✨", fontSize = 24.sp)
                }
                FrameShape.ARCH.id -> {
                    Text(text = "🌿✨", fontSize = 24.sp)
                }
                else -> {
                    Text(text = "🎀✨", fontSize = 24.sp)
                }
            }
        }

        // 3. Hanging Charm Badge on Bottom-Right Chain
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-12).dp)
                .size(42.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccentLight, GoldAccent, Color(0xFFFFA07A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val charmEmoji = remember(charmType) {
                CharmType.values().find { it.id == charmType }?.emoji ?: "🐰"
            }
            Text(text = charmEmoji, fontSize = 20.sp)
        }

        // 4. Duration Badge (Top-Right)
        if (showDurationBadge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = showDurationBadge,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 5. Lock Icon Overlay if Locked
        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun drawCornerBracket(
    drawScope: DrawScope,
    cornerPos: Offset,
    size: Float,
    rotationDeg: Float,
    brush: Brush
) {
    drawScope.rotate(rotationDeg, pivot = cornerPos) {
        val path = Path().apply {
            moveTo(cornerPos.x, cornerPos.y)
            lineTo(cornerPos.x + size, cornerPos.y)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    cornerPos.x + size - 12f,
                    cornerPos.y,
                    cornerPos.x + size,
                    cornerPos.y + 12f
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cornerPos.x + 8f, cornerPos.y + 8f)
            lineTo(cornerPos.x, cornerPos.y + size)
            close()
        }
        drawPath(path, brush)
    }
}

private fun drawFrameShape(
    drawScope: DrawScope,
    shape: String,
    center: Offset,
    radius: Float,
    brush: Brush,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle
) {
    when (shape) {
        FrameShape.HEART.id -> {
            val path = Path().apply {
                val w = radius * 1.8f
                val h = radius * 1.8f
                val top = center.y - h * 0.45f
                moveTo(center.x, top + h * 0.3f)
                cubicTo(
                    center.x - w * 0.5f, top - h * 0.15f,
                    center.x - w * 0.55f, top + h * 0.5f,
                    center.x, top + h * 0.95f
                )
                cubicTo(
                    center.x + w * 0.55f, top + h * 0.5f,
                    center.x + w * 0.5f, top - h * 0.15f,
                    center.x, top + h * 0.3f
                )
                close()
            }
            drawScope.drawPath(path, brush, style = style)
        }
        FrameShape.STAR.id -> {
            val path = Path()
            val points = 8
            val innerRadius = radius * 0.65f
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) radius else innerRadius
                val angle = i * Math.PI / points - Math.PI / 2
                val x = (center.x + r * cos(angle)).toFloat()
                val y = (center.y + r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawScope.drawPath(path, brush, style = style)
        }
        FrameShape.ARCH.id -> {
            val path = Path().apply {
                val w = radius * 1.6f
                val h = radius * 1.9f
                val left = center.x - w / 2
                val top = center.y - h / 2
                val right = center.x + w / 2
                val bottom = center.y + h / 2
                moveTo(left, bottom)
                lineTo(left, top + w / 2)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(left, top, right, top + w),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(right, bottom)
                close()
            }
            drawScope.drawPath(path, brush, style = style)
        }
        FrameShape.CLOUD.id -> {
            val path = Path().apply {
                val r = radius * 0.5f
                addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - r, center.x - radius * 0.2f, center.y + r))
                addOval(androidx.compose.ui.geometry.Rect(center.x - radius * 0.6f, center.y - radius * 0.9f, center.x + radius * 0.6f, center.y + radius * 0.3f))
                addOval(androidx.compose.ui.geometry.Rect(center.x + radius * 0.2f, center.y - r, center.x + radius, center.y + r))
            }
            drawScope.drawPath(path, brush, style = style)
        }
        FrameShape.ROUNDED.id -> {
            drawScope.drawRoundRect(
                brush = brush,
                topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f),
                size = Size(radius * 1.7f, radius * 1.7f),
                cornerRadius = CornerRadius(radius * 0.4f, radius * 0.4f),
                style = style
            )
        }
        else -> {
            // Petal / Floral frame (5-leaf clover petal cutout)
            val path = Path()
            val petals = 5
            for (i in 0 until petals) {
                val angle = (i * 2 * Math.PI / petals) - Math.PI / 2
                val petalCenter = Offset(
                    (center.x + radius * 0.45f * cos(angle)).toFloat(),
                    (center.y + radius * 0.45f * sin(angle)).toFloat()
                )
                path.addOval(
                    androidx.compose.ui.geometry.Rect(
                        petalCenter.x - radius * 0.55f,
                        petalCenter.y - radius * 0.55f,
                        petalCenter.x + radius * 0.55f,
                        petalCenter.y + radius * 0.55f
                    )
                )
            }
            drawScope.drawPath(path, brush, style = style)
        }
    }
}

private fun drawAnimatedParticles(
    drawScope: DrawScope,
    animationType: String,
    center: Offset,
    phase: Float,
    isPlaying: Boolean,
    primaryColor: Color
) {
    with(drawScope) {
        val speedMult = if (isPlaying) 1.8f else 1f

        when (animationType) {
            BoxAnimationType.PETALS.id -> {
                // 6 drifting sakura petals
                for (i in 0 until 6) {
                    val offsetPhase = (phase * speedMult + i * 60f) % 360f
                    val progress = offsetPhase / 360f
                    val x = center.x + (cos((offsetPhase + i * 45f) * Math.PI.toFloat() / 180f) * 90.dp.toPx())
                    val y = center.y - 80.dp.toPx() + (progress * 160.dp.toPx())
                    val alpha = sin(progress * Math.PI.toFloat()).coerceIn(0.2f, 0.85f)

                    drawCircle(
                        color = SoftPink.copy(alpha = alpha),
                        radius = (3.5f + (i % 3) * 1.5f).dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
            BoxAnimationType.TWINKLE.id -> {
                // Twinkling 4-point stars
                for (i in 0 until 8) {
                    val twinklePhase = (phase * speedMult * 1.5f + i * 45f) % 360f
                    val brightness = sin(twinklePhase * Math.PI.toFloat() / 180f).coerceIn(0.1f, 1f)
                    val angle = (i * 45f) * Math.PI.toFloat() / 180f
                    val dist = 40.dp.toPx() + (i % 3) * 20.dp.toPx()
                    val x = center.x + (dist * cos(angle))
                    val y = center.y + (dist * sin(angle))

                    val starSize = 4.dp.toPx() * brightness
                    drawLine(
                        color = GoldAccent.copy(alpha = brightness),
                        start = Offset(x - starSize, y),
                        end = Offset(x + starSize, y),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = GoldAccent.copy(alpha = brightness),
                        start = Offset(x, y - starSize),
                        end = Offset(x, y + starSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            BoxAnimationType.FLOAT.id -> {
                // Floating dreamy orbs
                for (i in 0 until 5) {
                    val floatP = (phase * speedMult + i * 72f) % 360f
                    val yOffset = sin(floatP * Math.PI.toFloat() / 180f) * 20.dp.toPx()
                    val xOffset = cos((floatP + i * 30f) * Math.PI.toFloat() / 180f) * 60.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.7f), primaryColor.copy(alpha = 0f)),
                            center = Offset(center.x + xOffset, center.y + yOffset),
                            radius = 12.dp.toPx()
                        ),
                        radius = 12.dp.toPx(),
                        center = Offset(center.x + xOffset, center.y + yOffset)
                    )
                }
            }
            else -> {
                // Subtle shimmer sparkle dots
                for (i in 0 until 6) {
                    val shimmerP = (phase * speedMult + i * 60f) % 360f
                    val alpha = sin(shimmerP * Math.PI.toFloat() / 180f).coerceIn(0.2f, 0.9f)
                    val x = center.x + (cos((i * 60f) * Math.PI.toFloat() / 180f) * 75.dp.toPx())
                    val y = center.y + (sin((i * 60f) * Math.PI.toFloat() / 180f) * 75.dp.toPx())
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
