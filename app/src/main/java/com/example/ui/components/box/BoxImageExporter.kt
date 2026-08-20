package com.example.ui.components.box

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.data.model.RecordingEntity
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object BoxImageExporter {

    fun generateAndShareBoxImage(context: Context, recording: RecordingEntity) {
        try {
            val width = 1080
            val height = 1350
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val primaryColorInt = try {
                android.graphics.Color.parseColor(recording.primaryColorHex)
            } catch (e: Exception) {
                0xFF8B72DE.toInt()
            }

            // 1. Draw Canvas Background (Soft Pastel Gradient)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(0xFFFAF7FF.toInt(), 0xFFFFF0F5.toInt(), 0xFFF0E6FF.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // 2. Draw Decorative Clouds / Sparkles
            val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFD166.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawCircle(120f, 150f, 16f, starPaint)
            canvas.drawCircle(width - 140f, 200f, 12f, starPaint)
            canvas.drawCircle(180f, height - 200f, 14f, starPaint)

            // 3. Header Text: "Aesthetically ✨"
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B213A.toInt()
                textSize = 58f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Aesthetically ✨", width / 2f, 140f, titlePaint)

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF726885.toInt()
                textSize = 32f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Record your voice. Get art. Share a feeling.", width / 2f, 195f, subtitlePaint)

            // 4. Draw Main 3D Aesthetic Music Box
            val boxLeft = 140f
            val boxTop = 260f
            val boxRight = width - 140f
            val boxBottom = boxTop + (boxRight - boxLeft)
            val boxRadius = 70f

            val boxShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x33000000
                maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawRoundRect(boxLeft, boxTop + 15f, boxRight, boxBottom + 15f, boxRadius, boxRadius, boxShadowPaint)

            val boxBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    boxLeft, boxTop, boxRight, boxBottom,
                    intArrayOf(primaryColorInt, 0xFFFFF0F5.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(boxLeft, boxTop, boxRight, boxBottom, boxRadius, boxRadius, boxBodyPaint)

            // Outer Stitches
            val stitchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x88FFFFFF.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 5f
                pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 0f)
            }
            canvas.drawRoundRect(boxLeft + 30f, boxTop + 30f, boxRight - 30f, boxBottom - 30f, boxRadius - 20f, boxRadius - 20f, stitchPaint)

            // Center Dark Window
            val windowRadius = 200f
            val centerX = (boxLeft + boxRight) / 2f
            val centerY = (boxTop + boxBottom) / 2f + 20f

            val goldFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFD166.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, windowRadius + 15f, goldFramePaint)

            val windowDarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    centerX, centerY, windowRadius,
                    intArrayOf(0xFF2D1B4E.toInt(), 0xFF120822.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(centerX, centerY, windowRadius, windowDarkPaint)

            // Draw Sound Waveform inside
            val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFAAC1.toInt()
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
            }
            val numBars = 16
            val barSpacing = 18f
            val totalWaveW = numBars * barSpacing
            val startWaveX = centerX - totalWaveW / 2f

            for (i in 0 until numBars) {
                val x = startWaveX + i * barSpacing
                val amp = 0.3f + 0.6f * sin((i * 0.4).toDouble()).toFloat().coerceAtLeast(0.1f)
                val barH = (windowRadius * 0.9f) * amp
                canvas.drawLine(x, centerY - barH / 2f, x, centerY + barH / 2f, wavePaint)
            }

            // 5. Recording Info Footer: Title, Date, Duration
            val recTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B213A.toInt()
                textSize = 48f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(recording.title, width / 2f, boxBottom + 90f, recTitlePaint)

            val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(recording.createdAt))
            val durationStr = String.format("%02d:%02d", (recording.durationMs / 1000) / 60, (recording.durationMs / 1000) % 60)
            
            val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF726885.toInt()
                textSize = 34f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$dateStr  |  Duration: $durationStr", width / 2f, boxBottom + 150f, infoPaint)

            // Watermark
            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFA59CB5.toInt()
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Made with Aesthetically 💖", width / 2f, height - 70f, watermarkPaint)

            // Save Bitmap to cache
            val cachePath = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val imageFile = File(cachePath, "voice_art_${recording.id}.png")
            val stream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            // Share via Intent
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "✨ Voice Art: \"${recording.title}\" recorded with Aesthetically! 🌸")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Aesthetic Voice Box"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback text share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "✨ Listen to \"${recording.title}\" with Aesthetically app!")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Voice Note"))
        }
    }
}
