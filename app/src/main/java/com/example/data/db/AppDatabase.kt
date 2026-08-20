package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RecordingEntity::class, TemplateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun templateDao(): TemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aesthetically_db"
                ).addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val PREMADE_TEMPLATES = listOf(
            TemplateEntity(
                id = "sakura_bloom",
                name = "Sakura Bloom",
                author = "Aesthetically Team",
                description = "Soft sakura petals and gentle spring breeze. Perfect for sweet, soft moments.",
                primaryColorHex = "#F48FB1",
                secondaryColorHex = "#FFF0F4",
                frameShape = FrameShape.PETAL.id,
                charmType = CharmType.BUNNY.id,
                animationType = BoxAnimationType.PETALS.id,
                isPremium = true,
                drawableResId = R.drawable.theme_sakura_bloom
            ),
            TemplateEntity(
                id = "moonlight_dream",
                name = "Moonlight Dream",
                author = "Aesthetically Team",
                description = "Quiet midnight whispers under crescent starlight and glowing constellations.",
                primaryColorHex = "#B39DDB",
                secondaryColorHex = "#EDE7F6",
                frameShape = FrameShape.ARCH.id,
                charmType = CharmType.MOON.id,
                animationType = BoxAnimationType.FLOAT.id,
                isPremium = false,
                drawableResId = R.drawable.theme_moonlight_dream
            ),
            TemplateEntity(
                id = "cottage_glow",
                name = "Cottage Glow",
                author = "Aesthetically Team",
                description = "Forest greenery, fairy lantern and cozy companion kitten warmth.",
                primaryColorHex = "#A5D6A7",
                secondaryColorHex = "#E8F5E9",
                frameShape = FrameShape.ARCH.id,
                charmType = CharmType.KITTY.id,
                animationType = BoxAnimationType.WAVE.id,
                isPremium = false,
                drawableResId = R.drawable.theme_cottage_glow
            ),
            TemplateEntity(
                id = "sunlit_latte",
                name = "Sunlit Latte",
                author = "Aesthetically Team",
                description = "Warm coffee aroma and cozy fireside memories with cute teddy.",
                primaryColorHex = "#D7CCC8",
                secondaryColorHex = "#EFEBE9",
                frameShape = FrameShape.ROUNDED.id,
                charmType = CharmType.TEDDY.id,
                animationType = BoxAnimationType.PULSE.id,
                isPremium = false,
                drawableResId = R.drawable.theme_sunlit_latte
            ),
            TemplateEntity(
                id = "celestial_starlight",
                name = "Celestial Starlight",
                author = "Aesthetically Team",
                description = "Cosmic violet hues and shimmering nebulae constellations.",
                primaryColorHex = "#90CAF9",
                secondaryColorHex = "#E3F2FD",
                frameShape = FrameShape.STAR.id,
                charmType = CharmType.SPARKLES.id,
                animationType = BoxAnimationType.TWINKLE.id,
                isPremium = true,
                drawableResId = R.drawable.app_icon_fg
            ),
            TemplateEntity(
                id = "y2k_cyber_heart",
                name = "Y2K Cyber Heart",
                author = "Aesthetically Team",
                description = "Retro candy pop aesthetics with holographic silk ribbons and hearts.",
                primaryColorHex = "#FF80AB",
                secondaryColorHex = "#FCE4EC",
                frameShape = FrameShape.HEART.id,
                charmType = CharmType.RIBBON.id,
                animationType = BoxAnimationType.RIBBON_GLOW.id,
                isPremium = false,
                drawableResId = R.drawable.hero_aesthetically
            )
        )

        private val INITIAL_RECORDINGS = listOf(
            RecordingEntity(
                id = "rec-1-morning",
                title = "Morning thoughts",
                createdAt = System.currentTimeMillis() - 86400000L * 2 + 3600000L * 3,
                durationMs = 83000L,
                audioFilePath = "sample_morning.m4a",
                templateId = "sakura_bloom",
                primaryColorHex = "#F48FB1",
                secondaryColorHex = "#FFF0F4",
                frameShape = FrameShape.PETAL.id,
                charmType = CharmType.BUNNY.id,
                animationType = BoxAnimationType.PETALS.id,
                isLocked = false,
                isFavorite = true,
                category = "Daily",
                note = "Felt super peaceful this morning watching the sunrise. Sending warmth.",
                waveformPoints = "0.2,0.4,0.7,0.5,0.9,0.6,0.8,0.4,0.7,0.9,0.6,0.3,0.5,0.8,0.7,0.3,0.6,0.8,0.4"
            ),
            RecordingEntity(
                id = "rec-2-night",
                title = "Late night talks",
                createdAt = System.currentTimeMillis() - 86400000L * 3 + 3600000L * 7,
                durationMs = 167000L,
                audioFilePath = "sample_night.m4a",
                templateId = "moonlight_dream",
                primaryColorHex = "#B39DDB",
                secondaryColorHex = "#EDE7F6",
                frameShape = FrameShape.ARCH.id,
                charmType = CharmType.MOON.id,
                animationType = BoxAnimationType.FLOAT.id,
                isLocked = false,
                isFavorite = true,
                category = "Thoughts",
                note = "Whispering under the starry sky before drifting to sleep.",
                waveformPoints = "0.1,0.3,0.5,0.8,0.6,0.9,0.7,0.4,0.6,0.8,0.5,0.7,0.6,0.9,0.4,0.6,0.5,0.2"
            ),
            RecordingEntity(
                id = "rec-3-joy",
                title = "Little joy",
                createdAt = System.currentTimeMillis() - 86400000L * 4 + 3600000L * 10,
                durationMs = 195000L,
                audioFilePath = "sample_joy.m4a",
                templateId = "cottage_glow",
                primaryColorHex = "#A5D6A7",
                secondaryColorHex = "#E8F5E9",
                frameShape = FrameShape.ARCH.id,
                charmType = CharmType.KITTY.id,
                animationType = BoxAnimationType.WAVE.id,
                isLocked = true,
                pinCode = "1234",
                isFavorite = false,
                category = "Letters",
                note = "A private voice letter locked with a gentle heart key.",
                waveformPoints = "0.3,0.5,0.8,0.6,0.7,0.9,0.8,0.6,0.4,0.7,0.9,0.5,0.6,0.8,0.7,0.4,0.5,0.3"
            ),
            RecordingEntity(
                id = "rec-4-coffee",
                title = "Coffee date",
                createdAt = System.currentTimeMillis() - 86400000L * 5 + 3600000L * 6,
                durationMs = 58000L,
                audioFilePath = "sample_coffee.m4a",
                templateId = "sunlit_latte",
                primaryColorHex = "#D7CCC8",
                secondaryColorHex = "#EFEBE9",
                frameShape = FrameShape.ROUNDED.id,
                charmType = CharmType.TEDDY.id,
                animationType = BoxAnimationType.PULSE.id,
                isLocked = false,
                isFavorite = false,
                category = "Daily",
                note = "Warm memories from the corner cafe with rain outside.",
                waveformPoints = "0.4,0.7,0.5,0.8,0.6,0.9,0.7,0.5,0.8,0.6,0.4,0.7,0.8,0.5,0.6,0.3,0.5,0.4"
            )
        )
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                database.templateDao().insertAll(PREMADE_TEMPLATES)
                for (rec in INITIAL_RECORDINGS) {
                    database.recordingDao().insertRecording(rec)
                }
            }
        }
    }
}
