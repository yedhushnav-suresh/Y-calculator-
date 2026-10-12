package com.example

import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.CalculatorException
import com.example.calculator.engine.NumberFormatMode
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    private fun assertClose(expected: Double, actual: Double, epsilon: Double = 1e-6) {
        assertTrue("Expected $expected but got $actual", abs(expected - actual) < epsilon)
    }

    @Test
    fun testBasicArithmetic() {
        assertClose(5.0, engine.evaluate("2+3"))
        assertClose(7.0, engine.evaluate("10-3"))
        assertClose(42.0, engine.evaluate("6×7"))
        assertClose(2.5, engine.evaluate("5÷2"))
    }

    @Test
    fun testOperatorPrecedence() {
        assertClose(14.0, engine.evaluate("2+3×4"))
        assertClose(20.0, engine.evaluate("(2+3)×4"))
        assertClose(11.0, engine.evaluate("3+4×2"))
        assertClose(17.0, engine.evaluate("5+3×2²"))
    }

    @Test
    fun testNegativeNumbersAndUnaryMinus() {
        assertClose(-5.0, engine.evaluate("-5"))
        assertClose(2.0, engine.evaluate("5+-3"))
        assertClose(8.0, engine.evaluate("5--3"))
        assertClose(-15.0, engine.evaluate("3×-5"))
    }

    @Test
    fun testPowersAndRoots() {
        assertClose(8.0, engine.evaluate("2^3"))
        assertClose(25.0, engine.evaluate("5²"))
        assertClose(8.0, engine.evaluate("2³"))
        assertClose(0.25, engine.evaluate("4⁻¹"))
        assertClose(3.0, engine.evaluate("√9"))
        assertClose(2.0, engine.evaluate("³√8"))
        assertClose(2.0, engine.evaluate("3x√8"))
    }

    @Test
    fun testTrigonometryInDegrees() {
        engine.angleMode = AngleMode.DEG
        assertClose(0.5, engine.evaluate("sin(30)"))
        assertClose(0.5, engine.evaluate("cos(60)"))
        assertClose(1.0, engine.evaluate("tan(45)"))
        assertClose(30.0, engine.evaluate("sin⁻¹(0.5)"))
        assertClose(60.0, engine.evaluate("cos⁻¹(0.5)"))
        assertClose(45.0, engine.evaluate("tan⁻¹(1)"))
    }

    @Test
    fun testTrigonometryInRadians() {
        engine.angleMode = AngleMode.RAD
        assertClose(0.0, engine.evaluate("sin(π)"))
        assertClose(-1.0, engine.evaluate("cos(π)"))
        assertClose(1.0, engine.evaluate("sin(π÷2)"))
    }

    @Test
    fun testHyperbolicTrig() {
        assertClose(1.175201, engine.evaluate("sinh(1)"), 1e-4)
        assertClose(1.543080, engine.evaluate("cosh(1)"), 1e-4)
        assertClose(0.761594, engine.evaluate("tanh(1)"), 1e-4)
        assertClose(1.0, engine.evaluate("asinh(1.175201)"), 1e-3)
    }

    @Test
    fun testFactorialAndCombinatorics() {
        assertClose(120.0, engine.evaluate("5!"))
        assertClose(1.0, engine.evaluate("0!"))
        assertClose(10.0, engine.evaluate("5nCr2"))
        assertClose(20.0, engine.evaluate("5nPr2"))
    }

    @Test
    fun testLogarithms() {
        assertClose(2.0, engine.evaluate("log(100)"))
        assertClose(1.0, engine.evaluate("ln(e)"))
        assertClose(3.0, engine.evaluate("log(1000)"))
    }

    @Test
    fun testFractions() {
        assertClose(0.5, engine.evaluate("1⌟2"))
        assertClose(0.8333333333, engine.evaluate("1⌟2+1⌟3"))
        val fracStr = engine.formatAsFraction(0.75)
        assertEquals("3⌟4", fracStr)
        val improperStr = engine.formatAsFraction(1.75, improper = true)
        assertEquals("7⌟4", improperStr)
        val mixedStr = engine.formatAsFraction(1.75, improper = false)
        assertEquals("1⌟3⌟4", mixedStr)
    }

    @Test
    fun testDegreeMinuteSeconds() {
        val dmsFormatted = engine.formatAsDms(12.5)
        assertEquals("12°30'0\"", dmsFormatted)
    }

    @Test
    fun testPolarRectangular() {
        engine.angleMode = AngleMode.DEG
        val r = engine.evaluate("Pol(3,4)")
        assertClose(5.0, r)
        val theta = engine.memory.getVariable('Y')
        assertClose(53.130102, theta, 1e-4)

        val x = engine.evaluate("Rec(5,53.130102)")
        assertClose(3.0, x, 1e-4)
        val y = engine.memory.getVariable('Y')
        assertClose(4.0, y, 1e-4)
    }

    @Test
    fun testImplicitMultiplication() {
        assertClose(6.0, engine.evaluate("2(3)"))
        assertClose(1.0, engine.evaluate("2sin(30)"))
        assertClose(6.2831853, engine.evaluate("2π"))
    }

    @Test
    fun testVariablesAndAns() {
        engine.evaluate("15+5")
        assertClose(20.0, engine.memory.ans)
        assertClose(40.0, engine.evaluate("Ans×2"))

        engine.memory.setVariable('A', 7.0)
        assertClose(14.0, engine.evaluate("2A"))
        assertClose(21.0, engine.evaluate("3A"))
    }

    @Test
    fun testStatisticsSdMode() {
        val stat = engine.statMemory
        stat.clear()
        stat.addData(10.0)
        stat.addData(20.0)
        stat.addData(30.0)

        assertEquals(3L, stat.count)
        assertClose(60.0, stat.sumX)
        assertClose(1400.0, stat.sumX2)
        assertClose(20.0, stat.mean)
        assertClose(8.164965, stat.popStdDev, 1e-4)
        assertClose(10.0, stat.sampleStdDev, 1e-4)
    }

    @Test
    fun testEngineeringNotation() {
        val engStr = engine.formatEngineering(12345.0)
        assertTrue("Engineering format should contain 10^", engStr.contains("×10^"))
    }

    @Test
    fun testFixAndSciFormatting() {
        engine.numberFormatMode = NumberFormatMode.FIX
        engine.fixDigits = 3
        assertEquals("3.142", engine.formatResult(3.14159))

        engine.numberFormatMode = NumberFormatMode.SCI
        engine.sciDigits = 4
        val sciStr = engine.formatResult(1234.5)
        assertTrue(sciStr.contains("1.23") && sciStr.contains("×10^"))
    }

    @Test
    fun testDivisionByZeroThrowsMathError() {
        try {
            engine.evaluate("5÷0")
            fail("Expected MathError on division by zero")
        } catch (e: CalculatorException.MathError) {
            // Success
        }
    }

    @Test
    fun testNegativeSquareRootThrowsMathError() {
        try {
            engine.evaluate("√-4")
            fail("Expected MathError on square root of negative")
        } catch (e: CalculatorException.MathError) {
            // Success
        }
    }
}
