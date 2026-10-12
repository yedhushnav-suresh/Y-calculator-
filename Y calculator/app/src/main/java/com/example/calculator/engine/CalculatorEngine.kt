package com.example.calculator.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class CalculatorEngine(
    val memory: CalculatorMemory = CalculatorMemory(),
    val statMemory: StatMemory = StatMemory()
) {
    var angleMode: AngleMode = AngleMode.DEG
    var numberFormatMode: NumberFormatMode = NumberFormatMode.NORM1
    var fixDigits: Int = 2
    var sciDigits: Int = 3
    var fractionDisplayMode: FractionDisplayMode = FractionDisplayMode.MIXED

    // Engineering exponent shift state (0 = normal, +1 = shift 1 step (10^3), etc.)
    var engOffset: Int = 0

    // Coordinate conversion last results (for Pol / Rec)
    var lastCoordinateResult: Pair<Double, Double>? = null

    /**
     * Evaluates a mathematical expression string.
     * Returns the evaluated Double value.
     */
    fun evaluate(expression: String): Double {
        if (expression.isBlank()) {
            throw CalculatorException.SyntaxError("Empty expression")
        }

        val tokens = tokenize(expression)
        if (tokens.isEmpty()) {
            throw CalculatorException.SyntaxError("Empty expression")
        }

        val rpn = parseToRpn(tokens)
        val result = evaluateRpn(rpn)

        if (result.isNaN()) {
            throw CalculatorException.MathError("Undefined result")
        }
        if (result.isInfinite()) {
            throw CalculatorException.MathError("Overflow / Division by zero")
        }

        memory.ans = result
        engOffset = 0 // reset engineering shift on new evaluation
        return result
    }

    // --- Tokenizer ---

    sealed class Token {
        data class Number(val value: Double, val rawText: String = "") : Token()
        data class Identifier(val name: String) : Token()
        data class Operator(val symbol: String, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class PostfixOp(val symbol: String) : Token()
        data class PrefixFunc(val name: String) : Token()
        data class Variable(val name: Char) : Token()
        object LeftParen : Token()
        object RightParen : Token()
        object Comma : Token()
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = input.length

        // Helper to check what the previous token was
        fun lastToken(): Token? = tokens.lastOrNull()
        fun isExpectingUnaryOrPrefix(): Boolean {
            val last = lastToken() ?: return true
            return last is Token.Operator || last is Token.PrefixFunc || last is Token.LeftParen || last is Token.Comma
        }

        while (i < len) {
            val c = input[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            // Check multi-character functions and operators
            if (input.startsWith("sinh⁻¹", i) || input.startsWith("asinh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("asinh"))
                i += if (input.startsWith("sinh⁻¹", i)) 6 else 5
            } else if (input.startsWith("cosh⁻¹", i) || input.startsWith("acosh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("acosh"))
                i += if (input.startsWith("cosh⁻¹", i)) 6 else 5
            } else if (input.startsWith("tanh⁻¹", i) || input.startsWith("atanh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("atanh"))
                i += if (input.startsWith("tanh⁻¹", i)) 6 else 5
            } else if (input.startsWith("sin⁻¹", i) || input.startsWith("asin", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("asin"))
                i += if (input.startsWith("sin⁻¹", i)) 5 else 4
            } else if (input.startsWith("cos⁻¹", i) || input.startsWith("acos", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("acos"))
                i += if (input.startsWith("cos⁻¹", i)) 5 else 4
            } else if (input.startsWith("tan⁻¹", i) || input.startsWith("atan", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("atan"))
                i += if (input.startsWith("tan⁻¹", i)) 5 else 4
            } else if (input.startsWith("sinh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("sinh"))
                i += 4
            } else if (input.startsWith("cosh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("cosh"))
                i += 4
            } else if (input.startsWith("tanh", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("tanh"))
                i += 4
            } else if (input.startsWith("sin", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("sin"))
                i += 3
            } else if (input.startsWith("cos", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("cos"))
                i += 3
            } else if (input.startsWith("tan", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("tan"))
                i += 3
            } else if (input.startsWith("log", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("log"))
                i += 3
            } else if (input.startsWith("ln", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("ln"))
                i += 2
            } else if (input.startsWith("³√", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("cbrt"))
                i += 2
            } else if (input.startsWith("√", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("sqrt"))
                i += 1
            } else if (input.startsWith("Pol(", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("pol"))
                tokens.add(Token.LeftParen)
                i += 4
            } else if (input.startsWith("Rec(", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("rec"))
                tokens.add(Token.LeftParen)
                i += 4
            } else if (input.startsWith("Pol", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("pol"))
                i += 3
            } else if (input.startsWith("Rec", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("rec"))
                i += 3
            } else if (input.startsWith("Rnd", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.PrefixFunc("rnd"))
                i += 3
            } else if (input.startsWith("Ans", i)) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.Number(memory.ans, "Ans"))
                i += 3
            } else if (input.startsWith("Ran#", i)) {
                insertImplicitMulIfNeeded(tokens)
                val randVal = (0..999).random() / 1000.0
                tokens.add(Token.Number(randVal, "Ran#"))
                i += 4
            } else if (input.startsWith("nPr", i)) {
                tokens.add(Token.Operator("nPr", 4))
                i += 3
            } else if (input.startsWith("nCr", i)) {
                tokens.add(Token.Operator("nCr", 4))
                i += 3
            } else if (input.startsWith("x√", i)) {
                tokens.add(Token.Operator("xroot", 6, isRightAssociative = true))
                i += 2
            } else if (input.startsWith("×10^", i) || input.startsWith("×10ˣ", i)) {
                val advance = if (input.startsWith("×10^", i)) 4 else 4
                tokens.add(Token.Operator("EXP", 7, isRightAssociative = true))
                i += advance
            } else if (c == 'π') {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.Number(PI, "π"))
                i++
            } else if (c == 'e' && (i + 1 >= len || !input[i + 1].isLetter())) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.Number(E, "e"))
                i++
            } else if (c == '²') {
                tokens.add(Token.PostfixOp("²"))
                i++
            } else if (c == '³') {
                tokens.add(Token.PostfixOp("³"))
                i++
            } else if (input.startsWith("⁻¹", i)) {
                tokens.add(Token.PostfixOp("⁻¹"))
                i += 2
            } else if (c == '!') {
                tokens.add(Token.PostfixOp("!"))
                i++
            } else if (c == '%') {
                tokens.add(Token.PostfixOp("%"))
                i++
            } else if (c == '°') {
                tokens.add(Token.PostfixOp("°"))
                i++
            } else if (c == '⌟') {
                tokens.add(Token.Operator("⌟", 5))
                i++
            } else if (c == '^') {
                tokens.add(Token.Operator("^", 6, isRightAssociative = true))
                i++
            } else if (c == '×' || c == '*') {
                tokens.add(Token.Operator("×", 3))
                i++
            } else if (c == '÷' || c == '/') {
                tokens.add(Token.Operator("÷", 3))
                i++
            } else if (c == '+') {
                if (isExpectingUnaryOrPrefix()) {
                    tokens.add(Token.PrefixFunc("unary+"))
                } else {
                    tokens.add(Token.Operator("+", 2))
                }
                i++
            } else if (c == '-' || c == '−') {
                if (isExpectingUnaryOrPrefix()) {
                    tokens.add(Token.PrefixFunc("unary-"))
                } else {
                    tokens.add(Token.Operator("-", 2))
                }
                i++
            } else if (c == '(') {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.LeftParen)
                i++
            } else if (c == ')') {
                tokens.add(Token.RightParen)
                i++
            } else if (c == ',' || c == ';') {
                tokens.add(Token.Comma)
                i++
            } else if (c in "ABCDEFMXY" && (i + 1 >= len || !input[i + 1].isLetter())) {
                insertImplicitMulIfNeeded(tokens)
                tokens.add(Token.Variable(c))
                i++
            } else if (c.isDigit() || c == '.') {
                insertImplicitMulIfNeeded(tokens)
                val start = i
                var hasDot = (c == '.')
                i++
                while (i < len) {
                    val nc = input[i]
                    if (nc.isDigit()) {
                        i++
                    } else if (nc == '.' && !hasDot) {
                        hasDot = true
                        i++
                    } else {
                        break
                    }
                }
                val numStr = input.substring(start, i)
                val value = numStr.toDoubleOrNull()
                    ?: throw CalculatorException.SyntaxError("Invalid number format: $numStr")
                tokens.add(Token.Number(value, numStr))
            } else {
                throw CalculatorException.SyntaxError("Unrecognized character '$c'")
            }
        }

        return tokens
    }

    private fun insertImplicitMulIfNeeded(tokens: MutableList<Token>) {
        val last = tokens.lastOrNull() ?: return
        if (last is Token.Number || last is Token.Variable || last is Token.RightParen || last is Token.PostfixOp) {
            tokens.add(Token.Operator("×", 3))
        }
    }

    // --- Shunting Yard Parser ---

    private fun parseToRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val opStack = mutableListOf<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number, is Token.Variable -> {
                    output.add(token)
                }
                is Token.PrefixFunc -> {
                    opStack.add(token)
                }
                is Token.PostfixOp -> {
                    output.add(token)
                }
                is Token.Operator -> {
                    while (opStack.isNotEmpty()) {
                        val top = opStack.last()
                        if (top is Token.PrefixFunc) {
                            output.add(opStack.removeAt(opStack.size - 1))
                        } else if (top is Token.Operator) {
                            if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                                (token.isRightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(opStack.removeAt(opStack.size - 1))
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    opStack.add(token)
                }
                is Token.Comma -> {
                    while (opStack.isNotEmpty() && opStack.last() !is Token.LeftParen) {
                        output.add(opStack.removeAt(opStack.size - 1))
                    }
                    if (opStack.isEmpty()) {
                        throw CalculatorException.SyntaxError("Misplaced comma")
                    }
                }
                is Token.LeftParen -> {
                    opStack.add(token)
                }
                is Token.RightParen -> {
                    var foundLeft = false
                    while (opStack.isNotEmpty()) {
                        val top = opStack.removeAt(opStack.size - 1)
                        if (top is Token.LeftParen) {
                            foundLeft = true
                            break
                        } else {
                            output.add(top)
                        }
                    }
                    if (!foundLeft) {
                        throw CalculatorException.SyntaxError("Mismatched parentheses")
                    }
                    // If function was at top before left paren
                    if (opStack.isNotEmpty() && opStack.last() is Token.PrefixFunc) {
                        output.add(opStack.removeAt(opStack.size - 1))
                    }
                }
                else -> {}
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.removeAt(opStack.size - 1)
            if (top is Token.LeftParen || top is Token.RightParen) {
                // Auto-close missing parentheses at end of expression, as Casio calculators do
                continue
            }
            output.add(top)
        }

        return output
    }

    // --- RPN Evaluator ---

    private fun evaluateRpn(rpn: List<Token>): Double {
        val stack = mutableListOf<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.add(token.value)
                is Token.Variable -> stack.add(memory.getVariable(token.name))
                is Token.PostfixOp -> {
                    if (stack.isEmpty()) throw CalculatorException.SyntaxError("Missing operand for ${token.symbol}")
                    val a = stack.removeAt(stack.size - 1)
                    stack.add(evalPostfix(token.symbol, a))
                }
                is Token.PrefixFunc -> {
                    if (token.name == "pol" || token.name == "rec") {
                        if (stack.size < 2) throw CalculatorException.SyntaxError("2 arguments required for ${token.name}")
                        val y = stack.removeAt(stack.size - 1)
                        val x = stack.removeAt(stack.size - 1)
                        stack.add(evalTwoArgFunc(token.name, x, y))
                    } else {
                        if (stack.isEmpty()) throw CalculatorException.SyntaxError("Missing argument for ${token.name}")
                        val a = stack.removeAt(stack.size - 1)
                        stack.add(evalPrefixFunc(token.name, a))
                    }
                }
                is Token.Operator -> {
                    if (stack.size < 2) throw CalculatorException.SyntaxError("Missing operand for ${token.symbol}")
                    val b = stack.removeAt(stack.size - 1)
                    val a = stack.removeAt(stack.size - 1)
                    stack.add(evalBinaryOp(token.symbol, a, b))
                }
                else -> throw CalculatorException.SyntaxError("Unexpected token in evaluation")
            }
        }

        if (stack.size != 1) {
            throw CalculatorException.SyntaxError("Malformed expression")
        }

        return stack.first()
    }

    private fun evalBinaryOp(op: String, a: Double, b: Double): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "×" -> a * b
        "÷" -> {
            if (abs(b) < 1e-15) throw CalculatorException.MathError("Division by zero")
            a / b
        }
        "^" -> {
            if (a < 0 && b != floor(b)) throw CalculatorException.MathError("Negative base with fractional exponent")
            a.pow(b)
        }
        "xroot" -> {
            // a x√ b => b^(1/a)
            if (abs(a) < 1e-15) throw CalculatorException.MathError("0th root is undefined")
            if (b < 0 && (a.toLong() % 2L == 0L)) throw CalculatorException.MathError("Even root of negative number")
            if (b < 0) -((-b).pow(1.0 / a)) else b.pow(1.0 / a)
        }
        "EXP" -> a * 10.0.pow(b)
        "nPr" -> calcPermutations(a, b)
        "nCr" -> calcCombinations(a, b)
        "⌟" -> {
            // Fraction operator: a⌟b
            if (abs(b) < 1e-15) throw CalculatorException.MathError("Division by zero in fraction")
            a / b
        }
        else -> throw CalculatorException.SyntaxError("Unknown operator $op")
    }

    private fun evalPostfix(op: String, a: Double): Double = when (op) {
        "²" -> a * a
        "³" -> a * a * a
        "⁻¹" -> {
            if (abs(a) < 1e-15) throw CalculatorException.MathError("Division by zero (x⁻¹)")
            1.0 / a
        }
        "!" -> calcFactorial(a)
        "%" -> a / 100.0
        "°" -> {
            // When ° is applied to a single number, it treats it as degrees
            a
        }
        else -> throw CalculatorException.SyntaxError("Unknown postfix operator $op")
    }

    private fun evalPrefixFunc(name: String, a: Double): Double = when (name) {
        "unary+" -> a
        "unary-" -> -a
        "sqrt" -> {
            if (a < 0) throw CalculatorException.MathError("Square root of negative number")
            sqrt(a)
        }
        "cbrt" -> cbrt(a)
        "log" -> {
            if (a <= 0) throw CalculatorException.MathError("log of non-positive number")
            log10(a)
        }
        "ln" -> {
            if (a <= 0) throw CalculatorException.MathError("ln of non-positive number")
            ln(a)
        }
        "sin" -> sin(toRadians(a))
        "cos" -> cos(toRadians(a))
        "tan" -> {
            val rad = toRadians(a)
            if (abs(cos(rad)) < 1e-14) throw CalculatorException.MathError("tan undefined at asymptote")
            tan(rad)
        }
        "asin" -> {
            if (a < -1.0 || a > 1.0) throw CalculatorException.MathError("asin domain error: |x| > 1")
            fromRadians(asin(a))
        }
        "acos" -> {
            if (a < -1.0 || a > 1.0) throw CalculatorException.MathError("acos domain error: |x| > 1")
            fromRadians(acos(a))
        }
        "atan" -> fromRadians(atan(a))
        "sinh" -> sinh(a)
        "cosh" -> cosh(a)
        "tanh" -> tanh(a)
        "asinh" -> asinh(a)
        "acosh" -> {
            if (a < 1.0) throw CalculatorException.MathError("acosh domain error: x < 1")
            acosh(a)
        }
        "atanh" -> {
            if (a <= -1.0 || a >= 1.0) throw CalculatorException.MathError("atanh domain error: |x| >= 1")
            atanh(a)
        }
        "rnd" -> roundToCurrentFormat(a)
        else -> throw CalculatorException.SyntaxError("Unknown function $name")
    }

    private fun evalTwoArgFunc(name: String, x: Double, y: Double): Double = when (name) {
        "pol" -> {
            // Pol(x, y): returns r, saves θ to Y
            val r = hypot(x, y)
            val theta = fromRadians(atan2(y, x))
            memory.setVariable('Y', theta)
            lastCoordinateResult = r to theta
            r
        }
        "rec" -> {
            // Rec(r, θ): returns x, saves y to Y
            val rad = toRadians(y)
            val resX = x * cos(rad)
            val resY = x * sin(rad)
            memory.setVariable('Y', resY)
            lastCoordinateResult = resX to resY
            resX
        }
        else -> throw CalculatorException.SyntaxError("Unknown 2-arg function $name")
    }

    // --- Trigonometry angle conversions ---

    fun toRadians(angle: Double): Double = when (angleMode) {
        AngleMode.DEG -> Math.toRadians(angle)
        AngleMode.RAD -> angle
        AngleMode.GRA -> angle * PI / 200.0
    }

    fun fromRadians(rad: Double): Double = when (angleMode) {
        AngleMode.DEG -> Math.toDegrees(rad)
        AngleMode.RAD -> rad
        AngleMode.GRA -> rad * 200.0 / PI
    }

    // --- Hyperbolic functions ---

    private fun asinh(x: Double): Double = ln(x + sqrt(x * x + 1.0))
    private fun acosh(x: Double): Double = ln(x + sqrt(x * x - 1.0))
    private fun atanh(x: Double): Double = 0.5 * ln((1.0 + x) / (1.0 - x))

    // --- Combinatorics & Factorial ---

    private fun calcFactorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw CalculatorException.MathError("Factorial of negative or non-integer")
        if (n > 69) throw CalculatorException.MathError("Factorial overflow")
        var result = 1.0
        val count = n.toInt()
        for (i in 2..count) {
            result *= i
        }
        return result
    }

    private fun calcPermutations(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || n != floor(n) || r != floor(r) || r > n) {
            throw CalculatorException.MathError("Invalid arguments for nPr")
        }
        if (n > 69) throw CalculatorException.MathError("nPr overflow")
        var result = 1.0
        val nInt = n.toLong()
        val rInt = r.toLong()
        for (i in (nInt - rInt + 1)..nInt) {
            result *= i
        }
        return result
    }

    private fun calcCombinations(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || n != floor(n) || r != floor(r) || r > n) {
            throw CalculatorException.MathError("Invalid arguments for nCr")
        }
        val nInt = n.toLong()
        val rInt = r.toLong()
        val k = min(rInt, nInt - rInt)
        var result = 1.0
        for (i in 1..k) {
            result = result * (nInt - k + i) / i
        }
        return round(result)
    }

    // --- Display & Formatting ---

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Math ERROR"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) < 1e-14) return "0"

        return when (numberFormatMode) {
            NumberFormatMode.FIX -> formatFix(value, fixDigits)
            NumberFormatMode.SCI -> formatSci(value, sciDigits)
            NumberFormatMode.NORM1 -> formatNorm(value, isNorm1 = true)
            NumberFormatMode.NORM2 -> formatNorm(value, isNorm2 = true)
        }
    }

    private fun formatFix(value: Double, digits: Int): String {
        val pattern = buildString {
            append("0")
            if (digits > 0) {
                append(".")
                repeat(digits) { append("0") }
            }
        }
        val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return df.format(value)
    }

    private fun formatSci(value: Double, sigDigits: Int): String {
        val pattern = buildString {
            append("0")
            if (sigDigits > 1) {
                append(".")
                repeat(sigDigits - 1) { append("0") }
            }
            append("E0")
        }
        val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val formatted = df.format(value)
        return convertScientificToCasio(formatted)
    }

    private fun formatNorm(value: Double, isNorm1: Boolean = false, isNorm2: Boolean = false): String {
        val absVal = abs(value)
        val exp = if (absVal == 0.0) 0 else floor(log10(absVal)).toInt()

        val useSci = if (isNorm1) {
            absVal < 0.01 || absVal >= 1e10
        } else {
            absVal < 1e-9 || absVal >= 1e10
        }

        return if (useSci) {
            formatSci(value, 10)
        } else {
            // Standard decimal representation up to 10 significant digits
            val df = DecimalFormat("#.##########", DecimalFormatSymbols(Locale.US))
            val res = df.format(value)
            if (res == "-0") "0" else res
        }
    }

    private fun convertScientificToCasio(sciStr: String): String {
        // e.g. "8.660254038E-1" -> mantissa "8.660254038", exp "-01"
        val parts = sciStr.split('E', 'e')
        if (parts.size != 2) return sciStr
        val mantissa = parts[0]
        val expVal = parts[1].toIntOrNull() ?: 0
        val expFormatted = if (expVal >= 0) "+%02d".format(expVal) else "-%02d".format(abs(expVal))
        return "$mantissa ×10^$expFormatted"
    }

    fun formatEngineering(value: Double): String {
        if (abs(value) < 1e-14) return "0"
        val absVal = abs(value)
        var exp = floor(log10(absVal)).toInt()
        var engExp = (exp / 3) * 3
        if (exp < 0 && exp % 3 != 0) {
            engExp -= 3
        }
        engExp += (engOffset * 3)

        val mantissa = value / 10.0.pow(engExp.toDouble())
        val df = DecimalFormat("#.#########", DecimalFormatSymbols(Locale.US))
        val mantissaStr = df.format(mantissa)
        val expFormatted = if (engExp >= 0) "+%02d".format(engExp) else "-%02d".format(abs(engExp))
        return "$mantissaStr ×10^$expFormatted"
    }

    fun formatAsFraction(value: Double, improper: Boolean = false): String? {
        val frac = Fraction.fromDouble(value) ?: return null
        return if (improper) frac.toImproperString() else frac.toMixedString()
    }

    fun formatAsDms(value: Double): String {
        val sign = if (value < 0) "-" else ""
        val absVal = abs(value)
        val degrees = floor(absVal).toLong()
        val remMinutes = (absVal - degrees) * 60.0
        val minutes = floor(remMinutes).toLong()
        val seconds = (remMinutes - minutes) * 60.0

        val secFormatted = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US)).format(seconds)
        return "$sign$degrees°$minutes'$secFormatted\""
    }

    private fun roundToCurrentFormat(value: Double): Double {
        return when (numberFormatMode) {
            NumberFormatMode.FIX -> {
                val factor = 10.0.pow(fixDigits.toDouble())
                round(value * factor) / factor
            }
            NumberFormatMode.SCI -> {
                val absVal = abs(value)
                if (absVal == 0.0) return 0.0
                val exp = floor(log10(absVal)).toInt()
                val factor = 10.0.pow((sciDigits - 1 - exp).toDouble())
                round(value * factor) / factor
            }
            else -> value
        }
    }
}
