package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val theme = if (state.isDarkTheme) CalculatorThemeColors.Dark else CalculatorThemeColors.WhiteSandal

    val monoFont = remember {
        try {
            FontFamily(Font(R.font.share_tech_mono))
        } catch (e: Exception) {
            FontFamily.Monospace
        }
    }

    // Outer screen background
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(theme.windowBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.TopCenter
    ) {
        val compact = maxHeight < 700.dp
        // Physical Calculator Shell
        Surface(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("calculator_body"),
            shape = RoundedCornerShape(16.dp),
            color = theme.shellBackground,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.shellBorder),
            shadowElevation = if (theme.isDark) 8.dp else 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // 1. Sleek Minimal Typography Header with Theme Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 22.dp else 26.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimalist App Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Y CALCULATOR",
                            color = theme.headerText,
                            fontSize = 11.5.sp,
                            fontFamily = monoFont,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "SCIENTIFIC",
                            color = theme.headerSubText,
                            fontSize = 8.5.sp,
                            fontFamily = monoFont,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }

                    // Two-Color Model Toggle (DARK / WHITE SANDAL)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.keyBorder.copy(alpha = 0.35f))
                            .border(1.dp, theme.keyBorder, RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { viewModel.toggleTheme() }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("theme_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isDarkTheme) "DARK ◐" else "WHITE ☼",
                            color = theme.headerText,
                            fontSize = 9.sp,
                            fontFamily = monoFont,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. LCD Display Screen (directly below header with clean margins)
                LcdScreen(
                    state = state,
                    theme = theme,
                    compact = compact,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Reasonable, consistent distance between LCD and keypad (NO giant gap!)
                Spacer(modifier = Modifier.height(8.dp))

                // 4. Complete Button Keypad
                // Note: passes extracted primitives to prevent keypad recomposition on expression edits
                val isMenuWaiting = state.modeMenuStep != 0 || state.clrMenuVisible ||
                        state.statSumMenuVisible || state.statVarMenuVisible || state.drgMenuVisible

                CalculatorKeypad(
                    isShift = state.isShiftActive,
                    isAlpha = state.isAlphaActive,
                    isMenuWaitingForNumber = isMenuWaiting,
                    theme = theme,
                    viewModel = viewModel,
                    compact = compact,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
