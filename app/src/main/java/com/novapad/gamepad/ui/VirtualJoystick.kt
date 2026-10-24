package com.novapad.gamepad.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onMove: (x: Float, y: Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val radius = 110f

    Canvas(
        modifier = modifier
            .size(160.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        thumbOffset = if (distance <= radius) {
                            newOffset
                        } else {
                            val angle = atan2(newOffset.y, newOffset.x)
                            Offset(cos(angle) * radius, sin(angle) * radius)
                        }
                        onMove(thumbOffset.x / radius, thumbOffset.y / radius)
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = Color(0x33FFFFFF),
            radius = radius,
            center = center
        )
        drawCircle(
            color = Color(0xFF64B5F6),
            radius = 38f,
            center = center + thumbOffset
        )
    }
}
