package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "aesthetically_settings")

class SettingsRepository(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("app_theme")
    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    private val KEY_MASTER_PIN = stringPreferencesKey("master_pin")
    private val KEY_BIOMETRIC = booleanPreferencesKey("biometric_enabled")
    private val KEY_MAX_DURATION_MIN = intPreferencesKey("max_duration_min")
    private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

    val selectedTheme: Flow<AppThemePreset> = context.dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME] ?: AppThemePreset.CELESTIAL.name
        try {
            AppThemePreset.valueOf(name)
        } catch (e: Exception) {
            AppThemePreset.CELESTIAL
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    val masterPin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MASTER_PIN] ?: ""
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC] ?: false
    }

    val maxDurationMin: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_MAX_DURATION_MIN] ?: 10
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setTheme(theme: AppThemePreset) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = isDark
        }
    }

    suspend fun setMasterPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MASTER_PIN] = pin
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC] = enabled
        }
    }

    suspend fun setMaxDuration(min: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MAX_DURATION_MIN] = min
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = done
        }
    }
}
