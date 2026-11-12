package com.keepfocus.app.util

class CounterEngine(private val debounceMs: Long = 700L, private val resetAfterMs: Long = 20_000L) {
    var count: Int = 0; private set
    private var lastEvent = Long.MIN_VALUE
    private var lastActive = Long.MIN_VALUE
    fun onScroll(now: Long, threshold: Int): Boolean {
        if (lastActive != Long.MIN_VALUE && now - lastActive > resetAfterMs) count = 0
        lastActive = now
        if (lastEvent != Long.MIN_VALUE && now - lastEvent < debounceMs) return false
        lastEvent = now
        count += 1
        if (count >= threshold) { count = 0; return true }
        return false
    }
    fun reset() { count = 0; lastEvent = Long.MIN_VALUE; lastActive = Long.MIN_VALUE }
    fun resetIfInactive(now: Long) { if (lastActive != Long.MIN_VALUE && now - lastActive > resetAfterMs) reset() }
}
