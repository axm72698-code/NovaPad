package com.novapad.gamepad.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novapad.gamepad.BluetoothHidController

@Composable
fun GamepadScreen(controller: BluetoothHidController, isConnected: Boolean) {
    val view = LocalView.current
    val haptic = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NOVAPAD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Badge(
                containerColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935)
            ) {
                Text(
                    text = if (isConnected) "Connected" else "Waiting for Host...",
                    modifier = Modifier.padding(4.dp),
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VirtualJoystick(onMove = { x, y -> controller.setJoystick(x, y) })
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .width(280.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GamepadButton("L1", 4, controller, haptic)
            GamepadButton("R1", 5, controller, haptic)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(170.dp)
                .padding(bottom = 16.dp)
        ) {
            Box(Modifier.align(Alignment.TopCenter)) { GamepadButton("Y", 3, controller, haptic, Color(0xFFFFD54F)) }
            Box(Modifier.align(Alignment.CenterStart)) { GamepadButton("X", 2, controller, haptic, Color(0xFF4FC3F7)) }
            Box(Modifier.align(Alignment.CenterEnd)) { GamepadButton("B", 1, controller, haptic, Color(0xFFE57373)) }
            Box(Modifier.align(Alignment.BottomCenter)) { GamepadButton("A", 0, controller, haptic, Color(0xFF81C784)) }
        }
    }
}

@Composable
fun GamepadButton(
    label: String,
    buttonId: Int,
    controller: BluetoothHidController,
    onHaptic: () -> Unit,
    accentColor: Color = Color.White
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(if (label.contains("1")) RoundedCornerShape(10.dp) else CircleShape)
            .background(if (isPressed) accentColor else Color(0xFF262626))
            .pointerInput(buttonId) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onHaptic()
                        controller.setButton(buttonId, true)
                        tryAwaitRelease()
                        isPressed = false
                        controller.setButton(buttonId, false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
