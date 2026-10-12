package com.example.calculator.ui

import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorAppMode
import com.example.calculator.engine.FractionDisplayMode
import com.example.calculator.engine.NumberFormatMode

data class CalculatorState(
    val expression: String = "",
    val cursorPosition: Int = 0,
    val result: String = "0",
    val resultExponent: String? = null,
    val isShiftActive: Boolean = false,
    val isAlphaActive: Boolean = false,
    val isHypActive: Boolean = false,
    val isStoActive: Boolean = false,
    val isRclActive: Boolean = false,
    val hasMemory: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val numberFormatMode: NumberFormatMode = NumberFormatMode.NORM1,
    val appMode: CalculatorAppMode = CalculatorAppMode.COMP,
    val fractionDisplayMode: FractionDisplayMode = FractionDisplayMode.MIXED,
    val errorMessage: String? = null,
    val modeMenuStep: Int = 0, // 0 = none, 1 = COMP/SD/REG, 2 = Deg/Rad/Gra, 3 = Fix/Sci/Norm, 4 = Disp
    val clrMenuVisible: Boolean = false,
    val statSumMenuVisible: Boolean = false,
    val statVarMenuVisible: Boolean = false,
    val drgMenuVisible: Boolean = false,
    val isInsertMode: Boolean = false,
    val isPoweredOn: Boolean = true,
    val statDataCount: Long = 0L,
    val statusNotification: String? = null,
    val isDarkTheme: Boolean = true
)
