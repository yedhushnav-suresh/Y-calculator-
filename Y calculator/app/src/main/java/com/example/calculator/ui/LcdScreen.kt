package com.example.calculator.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorAppMode
import com.example.calculator.engine.NumberFormatMode

@Composable
fun LcdScreen(
    state: CalculatorState,
    theme: CalculatorThemeColors,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val monoFont = remember {
        try {
            FontFamily(Font(R.font.share_tech_mono))
        } catch (e: Exception) {
            FontFamily.Monospace
        }
    }

    // Subtle, standard blinking cursor (530ms cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorVisible by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (theme.isDark) 2.dp else 1.5.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(theme.lcdBorder)
            .border(1.5.dp, theme.keyBorder, RoundedCornerShape(8.dp))
            .padding(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 96.dp else 112.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (state.isPoweredOn) theme.lcdBackground else theme.lcdBackground.copy(alpha = 0.6f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("lcd_screen"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!state.isPoweredOn) {
                // LCD is powered off (blank)
                Box(modifier = Modifier.fillMaxSize())
            } else {
                // 1. Dedicated Compact Status Indicator Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .testTag("lcd_status_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left group: Active modifier and memory status
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isShiftActive) StatusBadge(text = "SHIFT", color = theme.lcdPixelDark, font = monoFont)
                        if (state.isAlphaActive) StatusBadge(text = "ALPHA", color = theme.lcdPixelDark, font = monoFont)
                        if (state.isHypActive) StatusBadge(text = "HYP", color = theme.lcdPixelDark, font = monoFont)
                        if (state.hasMemory) StatusBadge(text = "M", color = theme.lcdPixelDark, font = monoFont)
                        if (state.isStoActive) StatusBadge(text = "STO", color = theme.lcdPixelDark, font = monoFont)
                        if (state.isRclActive) StatusBadge(text = "RCL", color = theme.lcdPixelDark, font = monoFont)
                        if (state.appMode == CalculatorAppMode.SD) StatusBadge(text = "STAT", color = theme.lcdPixelDark, font = monoFont)
                    }

                    // Right group: Active Angle mode, format mode, scroll indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = when (state.angleMode) {
                                AngleMode.DEG -> "DEG"
                                AngleMode.RAD -> "RAD"
                                AngleMode.GRA -> "GRA"
                            },
                            color = theme.lcdPixelDark,
                            font = monoFont
                        )
                        if (state.numberFormatMode == NumberFormatMode.FIX) StatusBadge(text = "FIX", color = theme.lcdPixelDark, font = monoFont)
                        if (state.numberFormatMode == NumberFormatMode.SCI) StatusBadge(text = "SCI", color = theme.lcdPixelDark, font = monoFont)
                        if (state.cursorPosition > 14) StatusBadge(text = "◀", color = theme.lcdPixelDark, font = monoFont)
                        if (state.expression.length > state.cursorPosition + 14) StatusBadge(text = "▶", color = theme.lcdPixelDark, font = monoFont)
                    }
                }

                // 2. Line 1: Expression Line or Menu Display
                val menuText = getMenuDisplayText(state)
                if (menuText != null) {
                    Text(
                        text = menuText,
                        color = theme.lcdPixelDark,
                        fontSize = 17.sp,
                        fontFamily = monoFont,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("lcd_menu_line")
                    )
                } else if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = theme.lcdPixelDark,
                        fontSize = 18.sp,
                        fontFamily = monoFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .testTag("lcd_error_line")
                    )
                } else {
                    // Expression with thin vertical insertion line cursor
                    val expr = state.expression
                    val pos = state.cursorPosition.coerceIn(0, expr.length)
                    val beforeCursor = expr.substring(0, pos)
                    val afterCursor = expr.substring(pos)
                    val scrollState = rememberScrollState()

                    LaunchedEffect(pos, expr.length) {
                        if (pos == expr.length) {
                            scrollState.scrollTo(scrollState.maxValue)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .horizontalScroll(scrollState)
                            .testTag("lcd_expression_line"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (beforeCursor.isNotEmpty()) {
                            Text(
                                text = beforeCursor,
                                color = theme.lcdPixelDark,
                                fontSize = 19.sp,
                                fontFamily = monoFont,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                        }

                        // Thin vertical insertion cursor: 1.5 dp wide, matching text height
                        if (cursorVisible > 0.5f) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 0.5.dp)
                                    .width(1.5.dp)
                                    .height(19.dp)
                                    .background(theme.lcdPixelDark)
                                    .testTag("cursor")
                            )
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .padding(horizontal = 0.5.dp)
                                    .width(1.5.dp)
                                    .height(19.dp)
                            )
                        }

                        if (afterCursor.isNotEmpty()) {
                            Text(
                                text = afterCursor,
                                color = theme.lcdPixelDark,
                                fontSize = 19.sp,
                                fontFamily = monoFont,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 3. Line 2: Result Line (Bold segmented digits, right aligned with exponent if any)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (state.errorMessage != null) {
                        Text(
                            text = "[AC]:Cancel  [◀][▶]:Goto",
                            color = theme.lcdPixelDark,
                            fontSize = 12.5.sp,
                            fontFamily = monoFont,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = state.result,
                                color = theme.lcdPixelDark,
                                fontSize = 27.sp,
                                fontFamily = monoFont,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.testTag("lcd_result_line")
                            )

                            // Scientific notation exponent (x10 ^ -01)
                            if (!state.resultExponent.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "×10",
                                        color = theme.lcdPixelDark,
                                        fontSize = 11.5.sp,
                                        fontFamily = monoFont,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = state.resultExponent,
                                        color = theme.lcdPixelDark,
                                        fontSize = 13.5.sp,
                                        fontFamily = monoFont,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    font: FontFamily,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = 9.5.sp,
        fontFamily = font,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = modifier
    )
}

private fun getMenuDisplayText(state: CalculatorState): String? {
    if (state.clrMenuVisible) {
        return "1:Scl  2:Mode  3:All"
    }
    if (state.statSumMenuVisible) {
        return "1:Σx²  2:Σx  3:n"
    }
    if (state.statVarMenuVisible) {
        return "1:x̄   2:xσn  3:xσn-1"
    }
    if (state.drgMenuVisible) {
        return "1:D   2:R   3:G"
    }
    return when (state.modeMenuStep) {
        1 -> "1:COMP  2:SD  3:REG"
        2 -> "1:Deg  2:Rad  3:Gra"
        3 -> "1:Fix  2:Sci  3:Norm"
        4 -> "1:a b/c  2:d/c"
        else -> null
    }
}
