package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorKeypad(
    isShift: Boolean,
    isAlpha: Boolean,
    isMenuWaitingForNumber: Boolean,
    theme: CalculatorThemeColors,
    viewModel: CalculatorViewModel,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val topSectionHeight = if (compact) 70.dp else 84.dp
    val smallKeyHeight = if (compact) 25.dp else 29.dp
    val functionKeyHeight = if (compact) 27.dp else 31.dp
    val numberKeyHeight = if (compact) 33.dp else 38.dp
    fun handleMenuOrDigit(digit: Int, digitStr: String) {
        if (isMenuWaitingForNumber) {
            val state = viewModel.uiState.value
            when {
                state.modeMenuStep != 0 -> viewModel.onModeSelect(digit)
                state.clrMenuVisible -> viewModel.onClrSelect(digit)
                state.statSumMenuVisible -> viewModel.onStatSumSelect(digit)
                state.statVarMenuVisible -> viewModel.onStatVarSelect(digit)
                state.drgMenuVisible -> viewModel.onDrgSelect(digit)
                else -> viewModel.insertText(digitStr)
            }
        } else {
            viewModel.insertText(digitStr)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // --- SECTION 1: Top Function Row & REPLAY Dial ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(topSectionHeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block (2 columns x 2 rows)
            Column(
                modifier = Modifier
                    .weight(1.85f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Row 1A: SHIFT, ALPHA
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorKey(
                        label = "SHIFT",
                        shiftLabel = "SHIFT",
                        keyType = KeyType.SHIFT,
                        theme = theme,
                        onClick = { viewModel.onShiftToggle() },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 9.5.sp
                    )
                    CalculatorKey(
                        label = "ALPHA",
                        alphaLabel = "ALPHA",
                        keyType = KeyType.ALPHA,
                        theme = theme,
                        onClick = { viewModel.onAlphaToggle() },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 9.5.sp
                    )
                }
                // Row 1B: x⁻¹, nCr
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorKey(
                        label = "x⁻¹",
                        shiftLabel = "x!",
                        theme = theme,
                        onClick = {
                            if (isShift) viewModel.insertText("!")
                            else viewModel.insertText("⁻¹")
                        },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 11.sp
                    )
                    CalculatorKey(
                        label = "nCr",
                        shiftLabel = "nPr",
                        theme = theme,
                        onClick = {
                            if (isShift) viewModel.insertText("nPr")
                            else viewModel.insertText("nCr")
                        },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 11.sp
                    )
                }
            }

            // Center: REPLAY D-PAD
            Box(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ReplayPad(
                    onLeft = { viewModel.onCursorLeft() },
                    onRight = { viewModel.onCursorRight() },
                    onUp = { viewModel.onHistoryUp() },
                    onDown = { viewModel.onHistoryDown() },
                    theme = theme
                )
            }

            // Right block (2 columns x 2 rows)
            Column(
                modifier = Modifier
                    .weight(1.85f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Row 1C: MODE, ON
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorKey(
                        label = "MODE",
                        shiftLabel = "CLR",
                        keyType = KeyType.SYSTEM,
                        theme = theme,
                        onClick = { viewModel.onModePressed() },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 9.sp
                    )
                    CalculatorKey(
                        label = "ON",
                        keyType = KeyType.SYSTEM,
                        theme = theme,
                        onClick = { viewModel.onPowerOn() },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 9.5.sp
                    )
                }
                // Row 1D: Pol(, x³
                Row(modifier = Modifier.fillMaxWidth()) {
                    CalculatorKey(
                        label = "Pol(",
                        shiftLabel = "Rec(",
                        alphaLabel = ":",
                        theme = theme,
                        onClick = {
                            when {
                                isShift -> viewModel.insertFunction("Rec")
                                isAlpha -> viewModel.insertText(":")
                                else -> viewModel.insertFunction("Pol")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 11.sp
                    )
                    CalculatorKey(
                        label = "x³",
                        shiftLabel = "³√",
                        theme = theme,
                        onClick = {
                            if (isShift) viewModel.insertFunction("³√")
                            else viewModel.insertText("³")
                        },
                        modifier = Modifier.weight(1f),
                        height = smallKeyHeight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // --- SECTION 2: Scientific Function Rows (6 columns each) ---

        // Row 2: a b/c, √, x², ^, log, ln
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "a b/c",
                shiftLabel = "d/c",
                theme = theme,
                onClick = { viewModel.onFractionKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 10.sp
            )
            CalculatorKey(
                label = "√",
                theme = theme,
                onClick = { viewModel.insertFunction("√") },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 13.sp
            )
            CalculatorKey(
                label = "x²",
                theme = theme,
                onClick = { viewModel.insertText("²") },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 12.sp
            )
            CalculatorKey(
                label = "^",
                shiftLabel = "x√",
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertText("x√")
                    else viewModel.insertText("^")
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 13.sp
            )
            CalculatorKey(
                label = "log",
                shiftLabel = "10ˣ",
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertText("10^")
                    else viewModel.insertFunction("log")
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
            CalculatorKey(
                label = "ln",
                shiftLabel = "eˣ",
                alphaLabel = "e",
                theme = theme,
                onClick = {
                    when {
                        isShift -> viewModel.insertText("e^")
                        isAlpha -> viewModel.insertText("e")
                        else -> viewModel.insertFunction("ln")
                    }
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
        }

        // Row 3: (-), ° ' ", hyp, sin, cos, tan
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "(-)",
                alphaLabel = "A",
                theme = theme,
                onClick = {
                    if (isAlpha) viewModel.handleVariableAccess('A')
                    else viewModel.insertText("-")
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.5.sp
            )
            CalculatorKey(
                label = "° ' \"",
                shiftLabel = "←",
                alphaLabel = "B",
                theme = theme,
                onClick = {
                    if (isAlpha) viewModel.handleVariableAccess('B')
                    else viewModel.onDmsKey()
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 10.5.sp
            )
            CalculatorKey(
                label = "hyp",
                alphaLabel = "C",
                theme = theme,
                onClick = {
                    if (isAlpha) viewModel.handleVariableAccess('C')
                    else viewModel.onHypKey()
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 10.5.sp
            )
            CalculatorKey(
                label = "sin",
                shiftLabel = "sin⁻¹",
                alphaLabel = "D",
                theme = theme,
                onClick = { viewModel.onSinKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
            CalculatorKey(
                label = "cos",
                shiftLabel = "cos⁻¹",
                alphaLabel = "E",
                theme = theme,
                onClick = { viewModel.onCosKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
            CalculatorKey(
                label = "tan",
                shiftLabel = "tan⁻¹",
                alphaLabel = "F",
                theme = theme,
                onClick = { viewModel.onTanKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
        }

        // Row 4: RCL, ENG, (, ), ,, M+
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "RCL",
                shiftLabel = "STO",
                theme = theme,
                onClick = { viewModel.onRclKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 9.5.sp
            )
            CalculatorKey(
                label = "ENG",
                shiftLabel = "←",
                theme = theme,
                onClick = { viewModel.onEngKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 9.5.sp
            )
            CalculatorKey(
                label = "(",
                theme = theme,
                onClick = { viewModel.insertText("(") },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 13.sp
            )
            CalculatorKey(
                label = ")",
                alphaLabel = "X",
                theme = theme,
                onClick = {
                    if (isAlpha) viewModel.handleVariableAccess('X')
                    else viewModel.insertText(")")
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 13.sp
            )
            CalculatorKey(
                label = ",",
                shiftLabel = ";",
                alphaLabel = "Y",
                theme = theme,
                onClick = {
                    when {
                        isAlpha -> viewModel.handleVariableAccess('Y')
                        isShift -> viewModel.insertText(";")
                        else -> viewModel.insertText(",")
                    }
                },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 13.sp
            )
            CalculatorKey(
                label = "M+",
                shiftLabel = "M-",
                alphaLabel = "M",
                statLabel = "DT",
                theme = theme,
                onClick = { viewModel.onMPlusKey() },
                modifier = Modifier.weight(1f),
                height = functionKeyHeight,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // --- SECTION 3: Main Numeric Keypad (5 columns each) ---

        // Row 5: 7, 8, 9, DEL, AC
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "7",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(7, "7") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "8",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(8, "8") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "9",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(9, "9") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "DEL",
                shiftLabel = "INS",
                keyType = KeyType.DEL,
                theme = theme,
                onClick = { viewModel.onDelete() },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 12.5.sp
            )
            CalculatorKey(
                label = "AC",
                shiftLabel = "OFF",
                keyType = KeyType.AC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.onPowerOff()
                    else viewModel.onAllClear()
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 13.5.sp
            )
        }

        // Row 6: 4, 5, 6, ×, ÷
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "4",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(4, "4") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "5",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(5, "5") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "6",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(6, "6") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "×",
                keyType = KeyType.SCIENTIFIC,
                theme = theme,
                onClick = { viewModel.insertText("×") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
            CalculatorKey(
                label = "÷",
                keyType = KeyType.SCIENTIFIC,
                theme = theme,
                onClick = { viewModel.insertText("÷") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
        }

        // Row 7: 1, 2, 3, +, −
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "1",
                statLabel = "[S-SUM]",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.onStatSumMenuOpen()
                    else handleMenuOrDigit(1, "1")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "2",
                statLabel = "[S-VAR]",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.onStatVarMenuOpen()
                    else handleMenuOrDigit(2, "2")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "3",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = { handleMenuOrDigit(3, "3") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "+",
                keyType = KeyType.SCIENTIFIC,
                theme = theme,
                onClick = { viewModel.insertText("+") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
            CalculatorKey(
                label = "−",
                keyType = KeyType.SCIENTIFIC,
                theme = theme,
                onClick = { viewModel.insertText("-") },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
        }

        // Row 8: 0, •, ×10ˣ, Ans, =
        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorKey(
                label = "0",
                shiftLabel = "Rnd",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertFunction("Rnd")
                    else viewModel.insertText("0")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 17.sp
            )
            CalculatorKey(
                label = "•",
                shiftLabel = "Ran#",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertText("Ran#")
                    else viewModel.insertText(".")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
            CalculatorKey(
                label = "×10ˣ",
                shiftLabel = "π",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertText("π")
                    else viewModel.insertText("×10^")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 11.sp
            )
            CalculatorKey(
                label = "Ans",
                shiftLabel = "DRG▶",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.onDrgMenuOpen()
                    else viewModel.insertText("Ans")
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 12.sp
            )
            CalculatorKey(
                label = "=",
                shiftLabel = "%",
                keyType = KeyType.NUMERIC,
                theme = theme,
                onClick = {
                    if (isShift) viewModel.insertText("%")
                    else viewModel.onEquals()
                },
                modifier = Modifier.weight(1f),
                height = numberKeyHeight,
                fontSize = 18.sp
            )
        }
    }
}
