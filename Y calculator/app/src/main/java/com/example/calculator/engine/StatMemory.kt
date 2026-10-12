package com.example.calculator.engine

import kotlin.math.sqrt

class StatMemory {
    private val dataPoints = mutableListOf<Pair<Double, Double>>() // (value, frequency)

    val count: Long
        get() = dataPoints.sumOf { it.second }.toLong()

    val sumX: Double
        get() = dataPoints.sumOf { it.first * it.second }

    val sumX2: Double
        get() = dataPoints.sumOf { it.first * it.first * it.second }

    val mean: Double
        get() {
            val n = count
            if (n == 0L) throw CalculatorException.MathError("No data points")
            return sumX / n.toDouble()
        }

    val popStdDev: Double
        get() {
            val n = count
            if (n == 0L) throw CalculatorException.MathError("No data points")
            val variance = (sumX2 - (sumX * sumX) / n.toDouble()) / n.toDouble()
            return sqrt(variance.coerceAtLeast(0.0))
        }

    val sampleStdDev: Double
        get() {
            val n = count
            if (n <= 1L) throw CalculatorException.MathError("n <= 1 for sample std dev")
            val variance = (sumX2 - (sumX * sumX) / n.toDouble()) / (n - 1).toDouble()
            return sqrt(variance.coerceAtLeast(0.0))
        }

    fun addData(x: Double, freq: Double = 1.0) {
        if (freq <= 0.0) throw CalculatorException.MathError("Frequency must be positive")
        dataPoints.add(x to freq)
    }

    fun removeLastData() {
        if (dataPoints.isNotEmpty()) {
            dataPoints.removeAt(dataPoints.size - 1)
        }
    }

    fun clear() {
        dataPoints.clear()
    }
}
