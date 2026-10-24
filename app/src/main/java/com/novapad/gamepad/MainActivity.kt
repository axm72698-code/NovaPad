package com.novapad.gamepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.novapad.gamepad.ui.GamepadScreen

class MainActivity : ComponentActivity() {
    private lateinit var hidController: BluetoothHidController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hidController = BluetoothHidController(this)

        setContent {
            val isConnected by hidController.isConnected.collectAsState()
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GamepadScreen(
                        controller = hidController,
                        connectionState = isConnected
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hidController.cleanup()
    }
}
