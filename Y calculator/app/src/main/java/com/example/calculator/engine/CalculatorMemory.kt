package com.example.calculator.engine

class CalculatorMemory {
    private val variables = mutableMapOf<Char, Double>(
        'A' to 0.0,
        'B' to 0.0,
        'C' to 0.0,
        'D' to 0.0,
        'E' to 0.0,
        'F' to 0.0,
        'M' to 0.0,
        'X' to 0.0,
        'Y' to 0.0
    )

    var ans: Double = 0.0

    val isMemoryNonZero: Boolean
        get() = (variables['M'] ?: 0.0) != 0.0

    fun getVariable(name: Char): Double {
        val upper = name.uppercaseChar()
        return variables[upper] ?: 0.0
    }

    fun setVariable(name: Char, value: Double) {
        val upper = name.uppercaseChar()
        if (variables.containsKey(upper)) {
            variables[upper] = value
        }
    }

    fun addToM(value: Double) {
        val current = variables['M'] ?: 0.0
        variables['M'] = current + value
    }

    fun subtractFromM(value: Double) {
        val current = variables['M'] ?: 0.0
        variables['M'] = current - value
    }

    fun clearVariables() {
        for (key in variables.keys.toList()) {
            variables[key] = 0.0
        }
    }

    fun clearAll() {
        clearVariables()
        ans = 0.0
    }
}
