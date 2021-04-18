package com.example.kotlin_app.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.*
import java.io.IOException

data class UserPreferences(
    val token: String
)

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private val TAG: String = "UserPreferencesRepo"

    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("token")
    }

    /**
     * Get the user preferences flow.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val token = preferences[PreferencesKeys.TOKEN] ?: ""
            UserPreferences(token)
        }

    suspend fun getToken(action: (value: String?) -> Unit) {
        userPreferencesFlow.collect {
            action(it.token)
        }
    }

    suspend fun updateToken(token: String?) {
        if (token != null) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.TOKEN] = token
            }
        }
    }

    suspend fun removeToken() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
        }
    }
}