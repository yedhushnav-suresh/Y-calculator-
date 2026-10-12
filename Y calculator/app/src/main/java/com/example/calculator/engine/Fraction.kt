package com.example.calculator.engine

import kotlin.math.abs
import kotlin.math.roundToLong

data class Fraction(val numerator: Long, val denominator: Long) {
    init {
        require(denominator != 0L) { "Denominator cannot be zero" }
    }

    val isNegative: Boolean = (numerator < 0) xor (denominator < 0)
    val absNumerator: Long = abs(numerator)
    val absDenominator: Long = abs(denominator)

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    fun reduce(): Fraction {
        val gcd = gcd(absNumerator, absDenominator)
        val sign = if (isNegative) -1L else 1L
        return Fraction(sign * (absNumerator / gcd), absDenominator / gcd)
    }

    fun toMixedString(): String {
        val reduced = reduce()
        val num = reduced.absNumerator
        val den = reduced.absDenominator
        val signStr = if (reduced.isNegative) "-" else ""

        if (den == 1L) {
            return "$signStr$num"
        }
        val whole = num / den
        val rem = num % den

        return if (whole > 0L && rem > 0L) {
            "$signStr$whole⌟$rem⌟$den"
        } else {
            "$signStr$num⌟$den"
        }
    }

    fun toImproperString(): String {
        val reduced = reduce()
        val num = reduced.absNumerator
        val den = reduced.absDenominator
        val signStr = if (reduced.isNegative) "-" else ""

        if (den == 1L) {
            return "$signStr$num"
        }
        return "$signStr$num⌟$den"
    }

    operator fun plus(other: Fraction): Fraction =
        Fraction(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator).reduce()

    operator fun minus(other: Fraction): Fraction =
        Fraction(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator).reduce()

    operator fun times(other: Fraction): Fraction =
        Fraction(numerator * other.numerator, denominator * other.denominator).reduce()

    operator fun div(other: Fraction): Fraction {
        require(other.numerator != 0L) { "Division by zero" }
        return Fraction(numerator * other.denominator, denominator * other.numerator).reduce()
    }

    companion object {
        fun gcd(a: Long, b: Long): Long {
            var x = a
            var y = b
            while (y != 0L) {
                val temp = y
                y = x % y
                x = temp
            }
            return if (x == 0L) 1L else x
        }

        /**
         * Convert a double to a rational fraction using continuous fraction algorithm
         * with precision suitable for calculator display.
         */
        fun fromDouble(value: Double, maxDenominator: Long = 100000L): Fraction? {
            if (value.isNaN() || value.isInfinite()) return null
            if (abs(value) > 1e10) return null

            val sign = if (value < 0) -1L else 1L
            val positiveVal = abs(value)

            // If it's already an integer
            val rounded = positiveVal.roundToLong()
            if (abs(positiveVal - rounded) < 1e-10) {
                return Fraction(sign * rounded, 1L)
            }

            var p0 = 0L
            var q0 = 1L
            var p1 = 1L
            var q1 = 0L

            var x = positiveVal
            for (i in 0..15) {
                val a = x.toLong()
                val p2 = a * p1 + p0
                val q2 = a * q1 + q0

                if (q2 > maxDenominator || q2 < 0L) break

                p0 = p1
                q0 = q1
                p1 = p2
                q1 = q2

                val diff = x - a
                if (diff < 1e-12) break
                x = 1.0 / diff

                val approx = p1.toDouble() / q1.toDouble()
                if (abs(approx - positiveVal) < 1e-9) {
                    return Fraction(sign * p1, q1).reduce()
                }
            }

            if (q1 in 1..maxDenominator) {
                val approx = p1.toDouble() / q1.toDouble()
                if (abs(approx - positiveVal) < 1e-7) {
                    return Fraction(sign * p1, q1).reduce()
                }
            }
            return null
        }
    }
}
