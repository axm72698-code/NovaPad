package com.novapad.gamepad.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novapad.gamepad.BluetoothHidController

private fun triggerVibration(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrator?.vibrate(30)
    }
}

@Composable
fun GamepadScreen(
    controller: BluetoothHidController,
    connectionState: Boolean
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F141C), Color(0xFF070A0E))
                )
            )
            .padding(16.dp)
    ) {
        // Status Bar (Top)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color(0xFF161B22), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (connectionState) Color(0xFF2EA043) else Color(0xFFDA3633))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (connectionState) "NovaPad: متصل" else "NovaPad: در انتظار اتصال",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Left Joystick Area
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            VirtualJoystick(
                onMove = { x, y ->
                    controller.sendReport(
                        (x * 127).toInt().toByte(),
                        (y * 127).toInt().toByte(),
                        0
                    )
                }
            )
        }

        // Right Action Buttons Area (X, Y, A, B)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .size(190.dp)
        ) {
            // Y Button (Top)
            ActionButton(
                text = "Y",
                color = Color(0xFFF1E05A),
                modifier = Modifier.align(Alignment.TopCenter),
                onPress = {
                    triggerVibration(context)
                    controller.sendReport(0, 0, 0x08)
                },
                onRelease = { controller.sendReport(0, 0, 0) }
            )

            // X Button (Left)
            ActionButton(
                text = "X",
                color = Color(0xFF58A6FF),
                modifier = Modifier.align(Alignment.CenterStart),
                onPress = {
                    triggerVibration(context)
                    controller.sendReport(0, 0, 0x04)
                },
                onRelease = { controller.sendReport(0, 0, 0) }
            )

            // B Button (Right)
            ActionButton(
                text = "B",
                color = Color(0xFFF85149),
                modifier = Modifier.align(Alignment.CenterEnd),
                onPress = {
                    triggerVibration(context)
                    controller.sendReport(0, 0, 0x02)
                },
                onRelease = { controller.sendReport(0, 0, 0) }
            )

            // A Button (Bottom)
            ActionButton(
                text = "A",
                color = Color(0xFF3FB950),
                modifier = Modifier.align(Alignment.BottomCenter),
                onPress = {
                    triggerVibration(context)
                    controller.sendReport(0, 0, 0x01)
                },
                onRelease = { controller.sendReport(0, 0, 0) }
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (isPressed) color.copy(alpha = 0.85f) else Color(0xFF21262D))
            .border(2.dp, color, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            }
    ) {
        Text(
            text = text,
            color = if (isPressed) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
