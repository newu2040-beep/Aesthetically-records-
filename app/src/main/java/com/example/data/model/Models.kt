package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class FrameShape(val id: String, val displayName: String) {
    PETAL("petal", "Sakura Petal"),
    HEART("heart", "Heart Window"),
    STAR("star", "Celestial Star"),
    ARCH("arch", "Vintage Arch"),
    CLOUD("cloud", "Fluffy Cloud"),
    ROUNDED("rounded", "Soft Rounded")
}

enum class CharmType(val id: String, val displayName: String, val emoji: String) {
    BUNNY("bunny", "Cute Bunny", "🐰"),
    KITTY("kitty", "Sweet Kitty", "🐱"),
    TEDDY("teddy", "Cozy Teddy", "🧸"),
    MOON("moon", "Crescent Moon", "🌙"),
    RIBBON("ribbon", "Silk Bow", "🎀"),
    SAKURA("sakura", "Cherry Blossom", "🌸"),
    SPARKLES("sparkles", "Star Sparkles", "✨"),
    HEART_LOCK("heart_lock", "Heart Locket", "💖")
}

enum class BoxAnimationType(val id: String, val displayName: String) {
    PETALS("petals", "Petals"),
    FLOAT("float", "Float"),
    PULSE("pulse", "Pulse"),
    WAVE("wave", "Wave"),
    TWINKLE("twinkle", "Twinkle"),
    RIBBON_GLOW("ribbon_glow", "Glow")
}

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long,
    val audioFilePath: String,
    val templateId: String = "sakura_bloom",
    val primaryColorHex: String = "#F48FB1",
    val secondaryColorHex: String = "#FFF0F4",
    val frameShape: String = FrameShape.PETAL.id,
    val charmType: String = CharmType.BUNNY.id,
    val animationType: String = BoxAnimationType.PETALS.id,
    val isLocked: Boolean = false,
    val pinCode: String = "",
    val isFavorite: Boolean = false,
    val category: String = "All",
    val note: String = "",
    val waveformPoints: String = "0.3,0.6,0.9,0.5,0.8,0.4,0.7,1.0,0.6,0.3,0.8,0.5,0.7,0.4,0.9,0.3",
    val sharedCode: String = ""
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val author: String = "Aesthetically Team",
    val description: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val frameShape: String,
    val charmType: String,
    val animationType: String,
    val isCustom: Boolean = false,
    val isPremium: Boolean = false,
    val isFavorite: Boolean = false,
    val drawableResId: Int = 0
)

data class LiveReaction(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val startXFraction: Float = (20..80).random() / 100f
)

data class ListeningParticipant(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val isOnline: Boolean = true,
    val currentPositionMs: Long = 0L,
    val isHost: Boolean = false
)
