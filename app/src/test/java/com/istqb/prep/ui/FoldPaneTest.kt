package com.istqb.prep.ui

import androidx.compose.ui.unit.IntRect
import org.junit.Assert.assertEquals
import org.junit.Test

class FoldPaneTest {
    private val window = IntRect(20, 40, 820, 640)

    @Test
    fun bookPostureUsesLargerSideAndExcludesHinge() {
        assertEquals(IntRect(20, 40, 410, 640), largerPane(window, IntRect(410, 40, 430, 640), true))
        assertEquals(IntRect(260, 40, 820, 640), largerPane(window, IntRect(250, 40, 260, 640), true))
        assertEquals(IntRect(20, 40, 420, 640), largerPane(window, IntRect(420, 40, 420, 640), true))
    }

    @Test
    fun tabletopPostureSupportsThinFoldsAndHingesWithThickness() {
        assertEquals(IntRect(20, 40, 820, 340), largerPane(window, IntRect(20, 340, 820, 340), false))
        assertEquals(IntRect(20, 265, 820, 640), largerPane(window, IntRect(20, 255, 820, 265), false))
    }

    @Test
    fun staleAndOutOfWindowReportsLeaveTheWindowUsable() {
        assertEquals(window, largerPane(window, IntRect(10, 40, 15, 640), true))
        assertEquals(window, largerPane(window, IntRect(410, 650, 430, 700), true))
        assertEquals(window, largerPane(window, IntRect(20, 660, 820, 680), false))
        assertEquals(window, largerPane(window, window, false))
    }
}
