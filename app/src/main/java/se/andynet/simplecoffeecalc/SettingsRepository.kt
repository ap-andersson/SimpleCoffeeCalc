package se.andynet.simplecoffeecalc

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val USER_PREFERENCES_NAME = "datastore_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

private val ratioKey = intPreferencesKey("ratio")
private val isWaterKey = booleanPreferencesKey("isWater")
private val weightKey = intPreferencesKey("weight")

data class Settings(val ratio: Int?, val isWater: Boolean?, val weight: Int?)

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    suspend fun readSettings(): Settings {
        val prefs = dataStore.data.first()
        return Settings(prefs[ratioKey], prefs[isWaterKey], prefs[weightKey])
    }

    suspend fun saveSettings(settings: Settings) {
        dataStore.edit { prefs ->
            settings.ratio?.let { prefs[ratioKey] = it }
            settings.isWater?.let { prefs[isWaterKey] = it }
            settings.weight?.let { prefs[weightKey] = it }
        }
    }
}
