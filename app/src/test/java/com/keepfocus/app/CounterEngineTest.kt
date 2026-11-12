package com.keepfocus.app

import com.keepfocus.app.util.CounterEngine
import org.junit.Assert.*
import org.junit.Test

class CounterEngineTest {
    @Test fun debouncesRapidScrolls() { val c=CounterEngine(); assertFalse(c.onScroll(1000,30)); assertFalse(c.onScroll(1500,30)); assertEquals(1,c.count) }
    @Test fun resetsAfterTwentySeconds() { val c=CounterEngine(); c.onScroll(1000,30); c.resetIfInactive(22001); assertEquals(0,c.count) }
    @Test fun firesAtThresholdAndResets() { val c=CounterEngine(); var fired=false; for(i in 0 until 3) fired=c.onScroll(1000L+i*800L,3); assertTrue(fired); assertEquals(0,c.count) }
}
