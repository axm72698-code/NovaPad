package com.novapad.gamepad.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novapad.gamepad.BluetoothHidController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamepadScreen(controller: BluetoothHidController) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    val triggerVibration: () -> Unit = {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NovaPad - Gamepad", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121212))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("آماده برای اتصال بلوتوث HID", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VirtualJoystick(
                        size = 160.dp,
                        onMove = { x, y ->
                            controller.sendJoystick(x, y)
                        }
                    )
                }

                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GamepadButton(
                        text = "Y",
                        color = Color(0xFFFFEB3B),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        triggerVibration()
                        controller.sendButtonPress(BluetoothHidController.BUTTON_Y)
                    }

                    GamepadButton(
                        text = "X",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        triggerVibration()
                        controller.sendButtonPress(BluetoothHidController.BUTTON_X)
                    }

                    GamepadButton(
                        text = "B",
                        color = Color(0xFFF44336),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        triggerVibration()
                        controller.sendButtonPress(BluetoothHidController.BUTTON_B)
                    }

                    GamepadButton(
                        text = "A",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        triggerVibration()
                        controller.sendButtonPress(BluetoothHidController.BUTTON_A)
                    }
                }
            }
        }
    }
}

@Composable
fun GamepadButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
