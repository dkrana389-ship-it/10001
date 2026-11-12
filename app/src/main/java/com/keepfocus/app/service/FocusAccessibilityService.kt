package com.keepfocus.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.keepfocus.app.data.FocusSettings
import com.keepfocus.app.data.Preferences
import com.keepfocus.app.data.Profile
import com.keepfocus.app.ui.MotivationActivity
import com.keepfocus.app.util.CounterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import com.keepfocus.app.data.StatsDao

@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val counter = CounterEngine()
    private lateinit var prefs: Preferences
    private lateinit var stats: StatsDao
    private var lastPackage: String? = null
    private var lastShortVideoAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val entry = EntryPointAccessors.fromApplication(applicationContext, ServiceEntryPoint::class.java)
        prefs = entry.preferences(); stats = entry.statsDao()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::prefs.isInitialized) return
        val packageName = event.packageName?.toString() ?: return
        val now = SystemClock.elapsedRealtime()
        if (packageName != lastPackage) {
            if (lastPackage != null) counter.reset()
            lastPackage = packageName
        }
        if (packageName != INSTAGRAM && packageName != YOUTUBE) { counter.reset(); return }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            lastShortVideoAt = now
            return
        }
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED && event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return
        // Scroll events are only accepted for the two explicitly allowed apps. Content descriptions/text
        // are intentionally not inspected or retained; UI identifiers are unstable across app releases.
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scope.launch {
                val setting = prefs.settings.first()
                if (!setting.enabled || (packageName == INSTAGRAM && !setting.instagram) || (packageName == YOUTUBE && !setting.youtube)) return@launch
                if (lastShortVideoAt != 0L && now - lastShortVideoAt > 20_000L) counter.reset()
                lastShortVideoAt = now
                if (counter.onScroll(now, setting.threshold)) {
                    val profile = prefs.profile.first()
                    showReminder(profile, counter.count + setting.threshold)
                }
            }
        }
    }

    private suspend fun showReminder(profile: Profile, sessionCount: Int) {
        val today = LocalDate.now().toString()
        val old = stats.get(today) ?: com.keepfocus.app.data.DailyStat(today)
        stats.put(old.copy(reminders = old.reminders + 1))
        val intent = Intent(this, MotivationActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("dream_line", profile.dreamLine.ifBlank { "${profile.nickname}, keep moving toward ${profile.dream}" })
            putExtra("nickname", profile.nickname)
            putExtra("photo", profile.photo)
            putExtra("session_count", sessionCount)
        }
        try { startActivity(intent) } catch (_: Exception) { /* Android may block background activity launches. */ }
    }
    override fun onInterrupt() { counter.reset() }
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
    companion object { const val INSTAGRAM="com.instagram.android"; const val YOUTUBE="com.google.android.youtube" }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ServiceEntryPoint { fun preferences(): Preferences; fun statsDao(): StatsDao }
