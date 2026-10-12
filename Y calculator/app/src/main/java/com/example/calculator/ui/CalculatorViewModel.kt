package com.example.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.engine.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

class CalculatorViewModel(
    val engine: CalculatorEngine = CalculatorEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    // History of entered expressions
    private val history = mutableListOf<String>()
    private var historyIndex = -1

    // Holds the last evaluated numeric result for display toggling (fraction, DMS, ENG)
    private var lastEvaluatedValue: Double? = 0.0

    // Whether result was just evaluated
    private var isResultFresh = false

    init {
        updateIndicators()
    }

    private fun updateIndicators() {
        _uiState.update { current ->
            current.copy(
                hasMemory = engine.memory.isMemoryNonZero,
                angleMode = engine.angleMode,
                numberFormatMode = engine.numberFormatMode,
                statDataCount = engine.statMemory.count
            )
        }
    }

    // --- Key Input Handlers ---

    fun onPowerOn() {
        _uiState.update {
            it.copy(
                isPoweredOn = true,
                expression = "",
                cursorPosition = 0,
                result = "0",
                resultExponent = null,
                errorMessage = null,
                isShiftActive = false,
                isAlphaActive = false,
                isHypActive = false,
                isStoActive = false,
                isRclActive = false,
                modeMenuStep = 0,
                clrMenuVisible = false,
                statSumMenuVisible = false,
                statVarMenuVisible = false,
                drgMenuVisible = false
            )
        }
        isResultFresh = false
    }

    fun onPowerOff() {
        _uiState.update { it.copy(isPoweredOn = false) }
    }

    fun onAllClear() {
        if (!_uiState.value.isPoweredOn) {
            onPowerOn()
            return
        }

        // Close any menus first
        if (dismissAnyActiveMenu()) return

        _uiState.update {
            it.copy(
                expression = "",
                cursorPosition = 0,
                errorMessage = null,
                result = "0",
                resultExponent = null,
                isShiftActive = false,
                isAlphaActive = false,
                isHypActive = false,
                isStoActive = false,
                isRclActive = false
            )
        }
        isResultFresh = false
    }

    fun onDelete() {
        val state = _uiState.value
        if (!state.isPoweredOn) return
        if (dismissAnyActiveMenu()) return

        val expr = state.expression
        val pos = state.cursorPosition
        if (pos > 0 && expr.isNotEmpty()) {
            val newExpr = expr.substring(0, pos - 1) + expr.substring(pos)
            _uiState.update {
                it.copy(
                    expression = newExpr,
                    cursorPosition = pos - 1,
                    errorMessage = null
                )
            }
        }
    }

    fun onShiftToggle() {
        if (!_uiState.value.isPoweredOn) return
        _uiState.update {
            it.copy(
                isShiftActive = !it.isShiftActive,
                isAlphaActive = false,
                isStoActive = false,
                isRclActive = false
            )
        }
    }

    fun onAlphaToggle() {
        if (!_uiState.value.isPoweredOn) return
        _uiState.update {
            it.copy(
                isAlphaActive = !it.isAlphaActive,
                isShiftActive = false,
                isStoActive = false,
                isRclActive = false
            )
        }
    }

    fun onModePressed() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (state.isShiftActive) {
            // Shift + MODE = CLR menu
            _uiState.update {
                it.copy(
                    isShiftActive = false,
                    clrMenuVisible = true,
                    modeMenuStep = 0,
                    statSumMenuVisible = false,
                    statVarMenuVisible = false
                )
            }
            return
        }

        // Cycle mode menu: 1 -> 2 -> 3 -> 4 -> 0
        val nextStep = if (state.modeMenuStep >= 4) 1 else state.modeMenuStep + 1
        _uiState.update {
            it.copy(
                modeMenuStep = nextStep,
                clrMenuVisible = false,
                statSumMenuVisible = false,
                statVarMenuVisible = false
            )
        }
    }

    fun onModeSelect(number: Int) {
        val step = _uiState.value.modeMenuStep
        when (step) {
            1 -> { // 1: COMP, 2: SD, 3: REG
                when (number) {
                    1 -> _uiState.update { it.copy(appMode = CalculatorAppMode.COMP, modeMenuStep = 0) }
                    2 -> _uiState.update { it.copy(appMode = CalculatorAppMode.SD, modeMenuStep = 0) }
                    3 -> _uiState.update { it.copy(appMode = CalculatorAppMode.REG, modeMenuStep = 0) }
                }
            }
            2 -> { // 1: Deg, 2: Rad, 3: Gra
                when (number) {
                    1 -> engine.angleMode = AngleMode.DEG
                    2 -> engine.angleMode = AngleMode.RAD
                    3 -> engine.angleMode = AngleMode.GRA
                }
                _uiState.update { it.copy(modeMenuStep = 0) }
                updateIndicators()
            }
            3 -> { // 1: Fix, 2: Sci, 3: Norm
                when (number) {
                    1 -> {
                        engine.numberFormatMode = NumberFormatMode.FIX
                        _uiState.update { it.copy(modeMenuStep = 0) }
                    }
                    2 -> {
                        engine.numberFormatMode = NumberFormatMode.SCI
                        _uiState.update { it.copy(modeMenuStep = 0) }
                    }
                    3 -> {
                        engine.numberFormatMode = NumberFormatMode.NORM1
                        _uiState.update { it.copy(modeMenuStep = 0) }
                    }
                }
                updateIndicators()
                refreshResultDisplay()
            }
            4 -> { // 1: Disp (ab/c or d/c)
                when (number) {
                    1 -> _uiState.update { it.copy(fractionDisplayMode = FractionDisplayMode.MIXED, modeMenuStep = 0) }
                    2 -> _uiState.update { it.copy(fractionDisplayMode = FractionDisplayMode.IMPROPER, modeMenuStep = 0) }
                }
                refreshResultDisplay()
            }
        }
    }

    fun onClrSelect(number: Int) {
        // 1: Scl, 2: Mode, 3: All
        when (number) {
            1 -> {
                engine.statMemory.clear()
            }
            2 -> {
                engine.angleMode = AngleMode.DEG
                engine.numberFormatMode = NumberFormatMode.NORM1
                _uiState.update { it.copy(appMode = CalculatorAppMode.COMP) }
            }
            3 -> {
                engine.statMemory.clear()
                engine.memory.clearAll()
                engine.angleMode = AngleMode.DEG
                engine.numberFormatMode = NumberFormatMode.NORM1
                _uiState.update { it.copy(appMode = CalculatorAppMode.COMP, expression = "", result = "0", resultExponent = null) }
                history.clear()
            }
        }
        _uiState.update { it.copy(clrMenuVisible = false) }
        updateIndicators()
    }

    fun onStatSumSelect(number: Int) {
        _uiState.update { it.copy(statSumMenuVisible = false) }
        try {
            val res = when (number) {
                1 -> engine.statMemory.sumX2
                2 -> engine.statMemory.sumX
                3 -> engine.statMemory.count.toDouble()
                else -> return
            }
            lastEvaluatedValue = res
            engine.memory.ans = res
            displayNumericResult(res)
        } catch (e: CalculatorException) {
            _uiState.update { it.copy(errorMessage = e.errorTitle) }
        }
    }

    fun onStatVarSelect(number: Int) {
        _uiState.update { it.copy(statVarMenuVisible = false) }
        try {
            val res = when (number) {
                1 -> engine.statMemory.mean
                2 -> engine.statMemory.popStdDev
                3 -> engine.statMemory.sampleStdDev
                else -> return
            }
            lastEvaluatedValue = res
            engine.memory.ans = res
            displayNumericResult(res)
        } catch (e: CalculatorException) {
            _uiState.update { it.copy(errorMessage = e.errorTitle) }
        }
    }

    fun onDrgSelect(number: Int) {
        _uiState.update { it.copy(drgMenuVisible = false) }
        val currentVal = lastEvaluatedValue ?: return
        val converted = when (number) {
            1 -> { // Convert from current mode to Degrees
                val rad = engine.toRadians(currentVal)
                Math.toDegrees(rad)
            }
            2 -> { // Convert to Radians
                engine.toRadians(currentVal)
            }
            3 -> { // Convert to Grads
                val rad = engine.toRadians(currentVal)
                rad * 200.0 / Math.PI
            }
            else -> currentVal
        }
        lastEvaluatedValue = converted
        engine.memory.ans = converted
        displayNumericResult(converted)
    }

    // --- Expression Editing & Character Insertion ---

    fun insertText(text: String) {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (dismissAnyActiveMenu()) return

        // If a result was just evaluated and user types an operator, continue from Ans
        var expr = state.expression
        var pos = state.cursorPosition

        if (isResultFresh) {
            if (isOperatorLeading(text)) {
                expr = "Ans"
                pos = 3
            } else {
                expr = ""
                pos = 0
            }
            isResultFresh = false
        }

        val newExpr = buildString {
            append(expr.substring(0, pos))
            append(text)
            append(expr.substring(pos))
        }

        _uiState.update {
            it.copy(
                expression = newExpr,
                cursorPosition = pos + text.length,
                errorMessage = null,
                isShiftActive = false,
                isAlphaActive = false
            )
        }
    }

    private fun isOperatorLeading(text: String): Boolean {
        return text in listOf("+", "-", "−", "×", "÷", "^", "²", "³", "⁻¹", "!", "%")
    }

    fun insertFunction(funcName: String) {
        insertText("$funcName(")
    }

    // --- Replay Navigation ---

    fun onCursorLeft() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        // If error is shown, pressing left goes back to expression to fix it
        if (state.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
            return
        }

        if (state.cursorPosition > 0) {
            _uiState.update { it.copy(cursorPosition = it.cursorPosition - 1) }
        }
    }

    fun onCursorRight() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (state.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
            return
        }

        if (state.cursorPosition < state.expression.length) {
            _uiState.update { it.copy(cursorPosition = it.cursorPosition + 1) }
        }
    }

    fun onHistoryUp() {
        if (history.isEmpty()) return
        if (historyIndex == -1) {
            historyIndex = history.size - 1
        } else if (historyIndex > 0) {
            historyIndex--
        }
        val pastExpr = history[historyIndex]
        _uiState.update {
            it.copy(
                expression = pastExpr,
                cursorPosition = pastExpr.length,
                errorMessage = null
            )
        }
        isResultFresh = false
    }

    fun onHistoryDown() {
        if (history.isEmpty() || historyIndex == -1) return
        if (historyIndex < history.size - 1) {
            historyIndex++
            val pastExpr = history[historyIndex]
            _uiState.update {
                it.copy(
                    expression = pastExpr,
                    cursorPosition = pastExpr.length,
                    errorMessage = null
                )
            }
        } else {
            historyIndex = -1
            _uiState.update {
                it.copy(
                    expression = "",
                    cursorPosition = 0,
                    errorMessage = null
                )
            }
        }
        isResultFresh = false
    }

    // --- Evaluation & Equals ---

    fun onEquals() {
        val state = _uiState.value
        if (!state.isPoweredOn) return
        if (dismissAnyActiveMenu()) return

        val expr = state.expression
        if (expr.isBlank()) {
            return
        }

        try {
            val result = engine.evaluate(expr)
            lastEvaluatedValue = result
            isResultFresh = true

            // Add to history if unique
            if (history.isEmpty() || history.last() != expr) {
                history.add(expr)
                if (history.size > 30) history.removeAt(0)
            }
            historyIndex = -1

            displayNumericResult(result)
        } catch (e: CalculatorException) {
            _uiState.update {
                it.copy(
                    errorMessage = e.errorTitle,
                    result = e.errorTitle,
                    resultExponent = null
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    errorMessage = "Syntax ERROR",
                    result = "Syntax ERROR",
                    resultExponent = null
                )
            }
        }
    }

    private fun displayNumericResult(value: Double) {
        val formatted = engine.formatResult(value)
        parseAndSetResultDisplay(formatted)
        updateIndicators()
    }

    private fun parseAndSetResultDisplay(formatted: String) {
        // e.g. "8.660254038 ×10^-01"
        if (formatted.contains("×10^")) {
            val parts = formatted.split("×10^")
            _uiState.update {
                it.copy(
                    result = parts[0].trim(),
                    resultExponent = parts[1].trim(),
                    errorMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    result = formatted,
                    resultExponent = null,
                    errorMessage = null
                )
            }
        }
    }

    private fun refreshResultDisplay() {
        val v = lastEvaluatedValue ?: return
        displayNumericResult(v)
    }

    // --- Special Key Handlers ---

    fun onFractionKey() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (state.isShiftActive) {
            // Shift + a b/c = d/c toggle (mixed to improper fraction or decimal)
            _uiState.update { it.copy(isShiftActive = false) }
            val v = lastEvaluatedValue ?: return
            val improper = (state.fractionDisplayMode == FractionDisplayMode.MIXED)
            val fracStr = engine.formatAsFraction(v, improper = improper)
            if (fracStr != null) {
                _uiState.update { it.copy(result = fracStr, resultExponent = null) }
            }
            return
        }

        // If in middle of typing expression, insert fraction separator ⌟
        if (!isResultFresh && state.expression.isNotEmpty()) {
            insertText("⌟")
        } else {
            // Toggle result fraction / decimal
            val v = lastEvaluatedValue ?: return
            val isCurrentFrac = state.result.contains("⌟")
            if (isCurrentFrac) {
                // Switch to decimal
                displayNumericResult(v)
            } else {
                val fracStr = engine.formatAsFraction(v, improper = false)
                if (fracStr != null) {
                    _uiState.update { it.copy(result = fracStr, resultExponent = null) }
                }
            }
        }
    }

    fun onDmsKey() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (!isResultFresh && state.expression.isNotEmpty()) {
            insertText("°")
        } else {
            val v = lastEvaluatedValue ?: return
            val isCurrentDms = state.result.contains("°")
            if (isCurrentDms) {
                displayNumericResult(v)
            } else {
                val dmsStr = engine.formatAsDms(v)
                _uiState.update { it.copy(result = dmsStr, resultExponent = null) }
            }
        }
    }

    fun onEngKey() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        val v = lastEvaluatedValue ?: return
        if (state.isShiftActive) {
            // Shift + ENG shifts back
            engine.engOffset--
            _uiState.update { it.copy(isShiftActive = false) }
        } else {
            engine.engOffset++
        }

        val formatted = engine.formatEngineering(v)
        parseAndSetResultDisplay(formatted)
    }

    fun onHypKey() {
        if (!_uiState.value.isPoweredOn) return
        _uiState.update { it.copy(isHypActive = !it.isHypActive) }
    }

    fun onSinKey() {
        val state = _uiState.value
        if (state.isAlphaActive) {
            handleAlphaVar('D')
            return
        }
        val isShift = state.isShiftActive
        val isHyp = state.isHypActive
        val fn = when {
            isShift && isHyp -> "sinh⁻¹"
            isShift -> "sin⁻¹"
            isHyp -> "sinh"
            else -> "sin"
        }
        _uiState.update { it.copy(isShiftActive = false, isHypActive = false) }
        insertFunction(fn)
    }

    fun onCosKey() {
        val state = _uiState.value
        if (state.isAlphaActive) {
            handleAlphaVar('E')
            return
        }
        val isShift = state.isShiftActive
        val isHyp = state.isHypActive
        val fn = when {
            isShift && isHyp -> "cosh⁻¹"
            isShift -> "cos⁻¹"
            isHyp -> "cosh"
            else -> "cos"
        }
        _uiState.update { it.copy(isShiftActive = false, isHypActive = false) }
        insertFunction(fn)
    }

    fun onTanKey() {
        val state = _uiState.value
        if (state.isAlphaActive) {
            handleAlphaVar('F')
            return
        }
        val isShift = state.isShiftActive
        val isHyp = state.isHypActive
        val fn = when {
            isShift && isHyp -> "tanh⁻¹"
            isShift -> "tan⁻¹"
            isHyp -> "tanh"
            else -> "tan"
        }
        _uiState.update { it.copy(isShiftActive = false, isHypActive = false) }
        insertFunction(fn)
    }

    fun onMPlusKey() {
        val state = _uiState.value
        if (!state.isPoweredOn) return

        if (state.isAlphaActive) {
            handleAlphaVar('M')
            return
        }

        // In SD mode, M+ is DT (Data Input)
        if (state.appMode == CalculatorAppMode.SD) {
            val expr = state.expression.ifBlank { "0" }
            try {
                val dataVal = if (isResultFresh && lastEvaluatedValue != null) {
                    lastEvaluatedValue!!
                } else {
                    engine.evaluate(expr)
                }
                engine.statMemory.addData(dataVal)
                val count = engine.statMemory.count
                _uiState.update {
                    it.copy(
                        expression = "",
                        cursorPosition = 0,
                        result = "n = $count",
                        resultExponent = null,
                        statDataCount = count
                    )
                }
                isResultFresh = true
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Math ERROR") }
            }
            return
        }

        // Standard M+ and M-
        val v = lastEvaluatedValue ?: 0.0
        if (state.isShiftActive) {
            engine.memory.subtractFromM(v)
            _uiState.update { it.copy(isShiftActive = false) }
        } else {
            engine.memory.addToM(v)
        }
        updateIndicators()
    }

    fun onStoKey() {
        if (!_uiState.value.isPoweredOn) return
        _uiState.update {
            it.copy(
                isStoActive = true,
                isRclActive = false,
                isShiftActive = false,
                isAlphaActive = false
            )
        }
    }

    fun onRclKey() {
        if (!_uiState.value.isPoweredOn) return
        if (_uiState.value.isShiftActive) {
            // Shift + RCL = STO
            onStoKey()
            return
        }
        _uiState.update {
            it.copy(
                isRclActive = true,
                isStoActive = false,
                isShiftActive = false,
                isAlphaActive = false
            )
        }
    }

    fun handleVariableAccess(varName: Char) {
        val state = _uiState.value
        if (state.isStoActive) {
            // Store Ans to variable
            val v = lastEvaluatedValue ?: engine.memory.ans
            engine.memory.setVariable(varName, v)
            _uiState.update {
                it.copy(
                    isStoActive = false,
                    result = "$varName = ${engine.formatResult(v)}",
                    resultExponent = null
                )
            }
            updateIndicators()
        } else if (state.isRclActive) {
            // Recall variable value
            val v = engine.memory.getVariable(varName)
            _uiState.update {
                it.copy(
                    isRclActive = false,
                    result = "$varName = ${engine.formatResult(v)}",
                    resultExponent = null
                )
            }
        } else {
            insertText(varName.toString())
        }
    }

    fun handleAlphaVar(varName: Char) {
        _uiState.update { it.copy(isAlphaActive = false) }
        insertText(varName.toString())
    }

    fun onStatSumMenuOpen() {
        _uiState.update { it.copy(isShiftActive = false, statSumMenuVisible = true) }
    }

    fun onStatVarMenuOpen() {
        _uiState.update { it.copy(isShiftActive = false, statVarMenuVisible = true) }
    }

    fun onDrgMenuOpen() {
        _uiState.update { it.copy(isShiftActive = false, drgMenuVisible = true) }
    }

    private fun dismissAnyActiveMenu(): Boolean {
        val state = _uiState.value
        if (state.modeMenuStep != 0 || state.clrMenuVisible || state.statSumMenuVisible ||
            state.statVarMenuVisible || state.drgMenuVisible
        ) {
            _uiState.update {
                it.copy(
                    modeMenuStep = 0,
                    clrMenuVisible = false,
                    statSumMenuVisible = false,
                    statVarMenuVisible = false,
                    drgMenuVisible = false
                )
            }
            return true
        }
        return false
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }
}
