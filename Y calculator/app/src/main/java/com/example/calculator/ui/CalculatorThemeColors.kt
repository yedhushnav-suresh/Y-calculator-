package com.example.calculator.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CalculatorThemeColors(
    val isDark: Boolean,
    val windowBackground: Color,
    val shellBackground: Color,
    val shellBorder: Color,
    val lcdBackground: Color,
    val lcdPixelDark: Color,
    val lcdBorder: Color,
    val keyScientificBg: Color,
    val keyNumericBg: Color,
    val keyBorder: Color,
    val keyText: Color,
    val keyDelAcText: Color,
    val labelShiftGold: Color,
    val labelAlphaPink: Color,
    val labelStatCyan: Color,
    val dpadBase: Color,
    val dpadCenter: Color,
    val dpadArrow: Color,
    val headerText: Color,
    val headerSubText: Color
) {
    companion object {
        // 1. Dark Charcoal Theme
        val Dark = CalculatorThemeColors(
            isDark = true,
            windowBackground = Color(0xFF111315),
            shellBackground = Color(0xFF1E2125),
            shellBorder = Color(0xFF2C3036),
            lcdBackground = Color(0xFF9EAA96),
            lcdPixelDark = Color(0xFF141914),
            lcdBorder = Color(0xFF282C30),
            keyScientificBg = Color(0xFF2B2F34),
            keyNumericBg = Color(0xFF3B4048),
            keyBorder = Color(0xFF4A515B),
            keyText = Color(0xFFF0F2F5),
            keyDelAcText = Color(0xFFFF5252),
            labelShiftGold = Color(0xFFF7C325),
            labelAlphaPink = Color(0xFFFF4081),
            labelStatCyan = Color(0xFF00E5FF),
            dpadBase = Color(0xFF262A2F),
            dpadCenter = Color(0xFF1F2226),
            dpadArrow = Color(0xFFA0A6B0),
            headerText = Color(0xFFD6DBE0),
            headerSubText = Color(0xFF7E8690)
        )

        // 2. White / Sandalwood Theme
        // "in white theme white and light yellow of sandal color ...back is white and black color"
        val WhiteSandal = CalculatorThemeColors(
            isDark = false,
            windowBackground = Color(0xFFECEEF2),
            shellBackground = Color(0xFFFFFFFF),
            shellBorder = Color(0xFFD8DCE2),
            lcdBackground = Color(0xFFF3E8CE), // Warm Sandalwood light-yellow
            lcdPixelDark = Color(0xFF151815),
            lcdBorder = Color(0xFFD9CFB6),
            keyScientificBg = Color(0xFFECEFF3),
            keyNumericBg = Color(0xFFFFFFFF),
            keyBorder = Color(0xFFD0D5DD),
            keyText = Color(0xFF121417),
            keyDelAcText = Color(0xFFD32F2F),
            labelShiftGold = Color(0xFFB87800), // Rich amber gold for light contrast
            labelAlphaPink = Color(0xFFC2185B), // Deep rose for light contrast
            labelStatCyan = Color(0xFF00796B),  // Deep teal cyan for light contrast
            dpadBase = Color(0xFFECEFF3),
            dpadCenter = Color(0xFFF8FAFC),
            dpadArrow = Color(0xFF424750),
            headerText = Color(0xFF181B1F),
            headerSubText = Color(0xFF6B7280)
        )
    }
}
