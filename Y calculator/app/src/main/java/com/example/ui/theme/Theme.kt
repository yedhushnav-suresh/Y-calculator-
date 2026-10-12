package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CalculatorColorScheme = darkColorScheme(
    primary = Color(0xFFF7C325),     // Shift gold
    secondary = Color(0xFFFF4081),   // Alpha pink
    tertiary = Color(0xFF00E5FF),    // Stat cyan
    background = Color(0xFF14171A),
    surface = Color(0xFF22262A),
    onPrimary = Color(0xFF14171A),
    onSecondary = Color.White,
    onBackground = Color(0xFFEDEFEF),
    onSurface = Color(0xFFEDEFEF)
)

@Composable
fun Fx82MsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CalculatorColorScheme,
        typography = Typography,
        content = content
    )
}
