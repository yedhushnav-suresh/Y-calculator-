package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReplayPad(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onUp: () -> Unit,
    onDown: () -> Unit,
    theme: CalculatorThemeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(if (theme.isDark) 2.dp else 1.5.dp, CircleShape)
            .clip(CircleShape)
            .background(theme.dpadBase)
            .border(1.5.dp, theme.keyBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Up Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.58f)
                .height(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 20.dp),
                    onClick = onUp
                )
                .testTag("dpad_up"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                color = theme.dpadArrow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Down Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.58f)
                .height(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 20.dp),
                    onClick = onDown
                )
                .testTag("dpad_down"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                color = theme.dpadArrow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Left Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight(0.58f)
                .width(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 20.dp),
                    onClick = onLeft
                )
                .testTag("dpad_left"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◀",
                color = theme.dpadArrow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.58f)
                .width(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 20.dp),
                    onClick = onRight
                )
                .testTag("dpad_right"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                color = theme.dpadArrow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Ring / Hub with "REPLAY" label
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .clip(CircleShape)
                .background(theme.dpadCenter)
                .border(1.dp, theme.keyBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "REPLAY",
                color = theme.dpadArrow,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
