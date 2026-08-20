package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemePreset(val displayName: String, val primaryColor: Color) {
    CELESTIAL("Celestial Lavender", CelestialPrimary),
    SAKURA("Sakura Blush", SakuraPrimary),
    MATCHA("Matcha Serenity", MatchaPrimary),
    LATTE("Sunlit Latte", LattePrimary),
    NEBULA("Midnight Nebula", NebulaPrimary)
}

fun getCustomColorScheme(preset: AppThemePreset, isDark: Boolean) = when (preset) {
    AppThemePreset.CELESTIAL -> if (isDark) {
        darkColorScheme(
            primary = CelestialDarkPrimary,
            onPrimary = Color.Black,
            primaryContainer = CelestialDarkSurfaceVariant,
            onPrimaryContainer = CelestialDarkPrimary,
            secondary = CelestialDarkSecondary,
            onSecondary = Color.Black,
            background = CelestialDarkBackground,
            onBackground = Color(0xFFECE6F5),
            surface = CelestialDarkSurface,
            onSurface = Color(0xFFECE6F5),
            surfaceVariant = CelestialDarkSurfaceVariant,
            onSurfaceVariant = Color(0xFFCFC6DF),
            outline = CelestialDarkOutline
        )
    } else {
        lightColorScheme(
            primary = CelestialPrimary,
            onPrimary = Color.White,
            primaryContainer = CelestialSurfaceVariant,
            onPrimaryContainer = CelestialPrimary,
            secondary = CelestialSecondary,
            onSecondary = Color.White,
            background = CelestialBackground,
            onBackground = TextPrimary,
            surface = CelestialSurface,
            onSurface = TextPrimary,
            surfaceVariant = CelestialSurfaceVariant,
            onSurfaceVariant = TextSecondary,
            outline = CelestialOutline
        )
    }
    AppThemePreset.SAKURA -> if (isDark) {
        darkColorScheme(
            primary = SakuraSecondary,
            onPrimary = Color.Black,
            secondary = SakuraTertiary,
            background = SakuraDarkBackground,
            surface = SakuraDarkSurface,
            surfaceVariant = Color(0xFF452232)
        )
    } else {
        lightColorScheme(
            primary = SakuraPrimary,
            onPrimary = Color.White,
            secondary = SakuraSecondary,
            background = SakuraBackground,
            surface = SakuraSurface,
            surfaceVariant = SakuraSurfaceVariant
        )
    }
    AppThemePreset.MATCHA -> if (isDark) {
        darkColorScheme(
            primary = MatchaSecondary,
            onPrimary = Color.Black,
            secondary = MatchaTertiary,
            background = MatchaDarkBackground,
            surface = MatchaDarkSurface,
            surfaceVariant = Color(0xFF283F2F)
        )
    } else {
        lightColorScheme(
            primary = MatchaPrimary,
            onPrimary = Color.White,
            secondary = MatchaSecondary,
            background = MatchaBackground,
            surface = MatchaSurface,
            surfaceVariant = MatchaSurfaceVariant
        )
    }
    AppThemePreset.LATTE -> if (isDark) {
        darkColorScheme(
            primary = LatteSecondary,
            onPrimary = Color.Black,
            secondary = LatteTertiary,
            background = LatteDarkBackground,
            surface = LatteDarkSurface,
            surfaceVariant = Color(0xFF42342A)
        )
    } else {
        lightColorScheme(
            primary = LattePrimary,
            onPrimary = Color.White,
            secondary = LatteSecondary,
            background = LatteBackground,
            surface = LatteSurface,
            surfaceVariant = LatteSurfaceVariant
        )
    }
    AppThemePreset.NEBULA -> darkColorScheme(
        primary = NebulaPrimary,
        onPrimary = Color.White,
        secondary = NebulaSecondary,
        tertiary = NebulaTertiary,
        background = NebulaBackground,
        surface = NebulaSurface,
        surfaceVariant = NebulaSurfaceVariant
    )
}

@Composable
fun AestheticallyTheme(
    preset: AppThemePreset = AppThemePreset.CELESTIAL,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = getCustomColorScheme(preset, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
