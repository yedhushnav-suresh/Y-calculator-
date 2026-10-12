package com.example.calculator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KeyType {
    SCIENTIFIC,
    NUMERIC,
    DEL,
    AC,
    SHIFT,
    ALPHA,
    SYSTEM
}

@Composable
fun CalculatorKey(
    label: String,
    onClick: () -> Unit,
    theme: CalculatorThemeColors,
    modifier: Modifier = Modifier,
    shiftLabel: String? = null,
    alphaLabel: String? = null,
    statLabel: String? = null,
    keyType: KeyType = KeyType.SCIENTIFIC,
    height: Dp = 33.dp,
    fontSize: TextUnit = 13.sp,
    testTag: String = label
) {
    val (bgColor, textColor) = when (keyType) {
        KeyType.NUMERIC -> theme.keyNumericBg to theme.keyText
        KeyType.DEL, KeyType.AC -> theme.keyScientificBg to theme.keyDelAcText
        KeyType.SHIFT -> theme.keyScientificBg to theme.labelShiftGold
        KeyType.ALPHA -> theme.keyScientificBg to theme.labelAlphaPink
        else -> theme.keyScientificBg to theme.keyText
    }

    // Single unified key cell: label container + button box
    Column(
        modifier = modifier
            .padding(horizontal = 1.5.dp, vertical = 0.5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Fixed-height secondary label row directly attached above button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left slot: SHIFT label or standalone STAT label
            val leftText = shiftLabel ?: (if (alphaLabel == null) statLabel else null)
            val leftColor = if (shiftLabel != null) theme.labelShiftGold else theme.labelStatCyan
            if (leftText != null) {
                Text(
                    text = leftText,
                    color = leftColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Start
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Middle slot: for special keys with 3 labels (e.g. M+ with M-, DT, M)
            if (shiftLabel != null && statLabel != null && alphaLabel != null) {
                Text(
                    text = statLabel,
                    color = theme.labelStatCyan,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            // Right slot: ALPHA label
            if (alphaLabel != null) {
                Text(
                    text = alphaLabel,
                    color = theme.labelAlphaPink,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.End
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }

        // 2. Physical Button
        val buttonShape = remember { RoundedCornerShape(4.dp) }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(buttonShape)
                .border(1.dp, theme.keyBorder, buttonShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = textColor.copy(alpha = 0.25f)),
                    onClick = onClick
                )
                .testTag("key_$testTag"),
            shape = buttonShape,
            color = bgColor,
            shadowElevation = if (theme.isDark) 1.dp else 1.5.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = if (keyType == KeyType.NUMERIC || keyType == KeyType.AC || keyType == KeyType.DEL) {
                        FontWeight.Bold
                    } else {
                        FontWeight.SemiBold
                    },
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
