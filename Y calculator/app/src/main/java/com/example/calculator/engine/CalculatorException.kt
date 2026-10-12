package com.example.calculator.engine

sealed class CalculatorException(val errorTitle: String, message: String) : Exception(message) {
    class MathError(message: String = "Math ERROR") : CalculatorException("Math ERROR", message)
    class SyntaxError(message: String = "Syntax ERROR") : CalculatorException("Syntax ERROR", message)
    class StackError(message: String = "Stack ERROR") : CalculatorException("Stack ERROR", message)
    class ArgError(message: String = "Arg ERROR") : CalculatorException("Arg ERROR", message)
}
