package com.keepfocus.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusStore by preferencesDataStore(name = "keep_focus_preferences")

@Singleton
class Preferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val name = stringPreferencesKey("name")
    private val nickname = stringPreferencesKey("nickname")
    private val dream = stringPreferencesKey("dream")
    private val dreamLine = stringPreferencesKey("dream_line")
    private val age = intPreferencesKey("age")
    private val profession = stringPreferencesKey("profession")
    private val photo = stringPreferencesKey("photo")
    private val enabled = booleanPreferencesKey("enabled")
    private val threshold = intPreferencesKey("threshold")
    private val instagram = booleanPreferencesKey("instagram")
    private val youtube = booleanPreferencesKey("youtube")
    private val setup = booleanPreferencesKey("setup")

    val profile: Flow<Profile> = context.focusStore.data.map { p -> Profile(p[name].orEmpty(), p[nickname].orEmpty(), p[dream].orEmpty(), p[dreamLine].orEmpty(), p[age] ?: 0, p[profession].orEmpty(), p[photo].orEmpty()) }
    val settings: Flow<FocusSettings> = context.focusStore.data.map { p -> FocusSettings(p[enabled] ?: false, p[threshold] ?: 30, p[instagram] ?: true, p[youtube] ?: true, p[setup] ?: false) }
    suspend fun saveProfile(p: Profile) { context.focusStore.edit { it[name]=p.name; it[nickname]=p.nickname; it[dream]=p.dream; it[dreamLine]=p.dreamLine; it[age]=p.age; it[profession]=p.profession; it[photo]=p.photo; it[setup]=true } }
    suspend fun updateSettings(s: FocusSettings) { context.focusStore.edit { it[enabled]=s.enabled; it[threshold]=s.threshold; it[instagram]=s.instagram; it[youtube]=s.youtube } }
}
data class Profile(val name:String="", val nickname:String="", val dream:String="", val dreamLine:String="", val age:Int=0, val profession:String="", val photo:String="")
data class FocusSettings(val enabled:Boolean=false, val threshold:Int=30, val instagram:Boolean=true, val youtube:Boolean=true, val setup:Boolean=false)
