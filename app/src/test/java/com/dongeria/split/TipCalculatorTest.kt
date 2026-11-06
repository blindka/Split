package com.dongeria.split

import org.junit.Assert.assertEquals
import org.junit.Test

class TipCalculatorTest {

    private val delta = 0.001

    // ---- calculateTip ----

    @Test
    fun calculateTip_basicPercentage() {
        val result = calculateTip(amount = 100.0, tipPercent = 10.0, roundUp = false)
        assertEquals(10.0, result.tip, delta)
        assertEquals(110.0, result.total, delta)
        assertEquals(110.0, result.perPerson, delta)
    }

    @Test
    fun calculateTip_decimalTip_noRounding() {
        val result = calculateTip(amount = 85.0, tipPercent = 12.0, roundUp = false)
        assertEquals(10.2, result.tip, delta)
    }

    @Test
    fun calculateTip_roundUp_alwaysRoundsUp() {
        // 10.2 should become 11, not 10
        val result = calculateTip(amount = 85.0, tipPercent = 12.0, roundUp = true)
        assertEquals(11.0, result.tip, delta)
        assertEquals(96.0, result.total, delta)
    }

    @Test
    fun calculateTip_roundUp_wholeNumberStaysTheSame() {
        val result = calculateTip(amount = 100.0, tipPercent = 10.0, roundUp = true)
        assertEquals(10.0, result.tip, delta)
    }

    @Test
    fun calculateTip_splitBetweenPeople() {
        val result = calculateTip(amount = 300.0, tipPercent = 10.0, roundUp = false, people = 3)
        assertEquals(330.0, result.total, delta)
        assertEquals(110.0, result.perPerson, delta)
    }

    @Test
    fun calculateTip_zeroPeople_treatedAsOne() {
        val result = calculateTip(amount = 100.0, tipPercent = 10.0, roundUp = false, people = 0)
        assertEquals(110.0, result.perPerson, delta)
    }

    @Test
    fun calculateTip_zeroAmount() {
        val result = calculateTip(amount = 0.0, tipPercent = 15.0, roundUp = true)
        assertEquals(0.0, result.tip, delta)
        assertEquals(0.0, result.total, delta)
    }

    // ---- sanitizeNumber ----

    @Test
    fun sanitizeNumber_removesInvalidCharacters() {
        assertEquals("123", sanitizeNumber("1a2-3 "))
    }

    @Test
    fun sanitizeNumber_keepsSingleDecimalPoint() {
        assertEquals("12.34", sanitizeNumber("12.3.4"))
    }

    @Test
    fun sanitizeNumber_limitsToTwoDecimals() {
        assertEquals("9.99", sanitizeNumber("9.999"))
    }

    @Test
    fun sanitizeNumber_commaBecomesDecimalPoint() {
        assertEquals("12.5", sanitizeNumber("12,5"))
    }
}
