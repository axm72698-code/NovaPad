package com.novapad.gamepad

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class BluetoothHidController(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private var hidDevice: BluetoothHidDevice? = null
    private var hostDevice: BluetoothDevice? = null

    private var buttonsState: Short = 0
    private var stickX: Byte = 0
    private var stickY: Byte = 0

    var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    private val hidDescriptor = byteArrayOf(
        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x05.toByte(),
        0xa1.toByte(), 0x01.toByte(),
        0xa1.toByte(), 0x00.toByte(),
        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x30.toByte(),
        0x09.toByte(), 0x31.toByte(),
        0x15.toByte(), 0x81.toByte(),
        0x25.toByte(), 0x7f.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x95.toByte(), 0x02.toByte(),
        0x81.toByte(), 0x02.toByte(),
        0x05.toByte(), 0x09.toByte(),
        0x19.toByte(), 0x01.toByte(),
        0x29.toByte(), 0x0c.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x95.toByte(), 0x0c.toByte(),
        0x81.toByte(), 0x02.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x95.toByte(), 0x04.toByte(),
        0x81.toByte(), 0x03.toByte(),
        0xc0.toByte(),
        0xc0.toByte()
    )

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            val isConnected = state == BluetoothProfile.STATE_CONNECTED
            if (isConnected) hostDevice = device else if (state == BluetoothProfile.STATE_DISCONNECTED) hostDevice = null
            onConnectionStateChanged?.invoke(isConnected)
        }
    }

    fun init() {
        adapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                    val sdp = BluetoothHidDeviceAppSdpSettings("NovaPad", "Virtual Gamepad", "Nova", BluetoothHidDevice.SUBCLASS1_COMBO, hidDescriptor)
                    val qos = BluetoothHidDeviceAppQosSettings(BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT, 800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX)
                    hidDevice?.registerApp(sdp, null, qos, Executors.newSingleThreadExecutor(), callback)
                }
            }
            override fun onServiceDisconnected(profile: Int) {
                hidDevice = null
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    fun setButton(buttonIndex: Int, isPressed: Boolean) {
        buttonsState = if (isPressed) {
            (buttonsState.toInt() or (1 shl buttonIndex)).toShort()
        } else {
            (buttonsState.toInt() and (1 shl buttonIndex).inv()).toShort()
        }
        sendReport()
    }

    fun setJoystick(x: Float, y: Float) {
        stickX = (x * 127).toInt().coerceIn(-127, 127).toByte()
        stickY = (y * 127).toInt().coerceIn(-127, 127).toByte()
        sendReport()
    }

    private fun sendReport() {
        val target = hostDevice ?: return
        val report = byteArrayOf(
            stickX,
            stickY,
            (buttonsState.toInt() and 0xFF).toByte(),
            ((buttonsState.toInt() shr 8) and 0xFF).toByte()
        )
        hidDevice?.sendReport(target, 0, report)
    }

    fun close() {
        hidDevice?.unregisterApp()
    }
}
