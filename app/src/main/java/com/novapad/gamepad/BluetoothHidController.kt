package com.novapad.gamepad

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothHidController(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var hidDevice: BluetoothHidDevice? = null
    private var targetDevice: BluetoothDevice? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val serviceListener = object : BluetoothProfile.ServiceListener {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                hidDevice = null
                _isConnected.value = false
            }
        }
    }

    private val callback = object : BluetoothHidDevice.Callback() {
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            targetDevice = if (state == BluetoothProfile.STATE_CONNECTED) device else null
            _isConnected.value = (state == BluetoothProfile.STATE_CONNECTED)
        }
    }

    init {
        bluetoothAdapter?.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        val sdp = BluetoothHidDeviceAppSdpSettings(
            "NovaPad Gamepad",
            "NovaPad Virtual HID Controller",
            "NovaPad",
            BluetoothHidDeviceAppSdpSettings.SUBCLASS1_COMBO,
            REPORT_DESCRIPTOR
        )
        val qos = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )
        hidDevice?.registerApp(sdp, qos, null, context.mainExecutor, callback)
    }

    @SuppressLint("MissingPermission")
    fun sendReport(x: Byte, y: Byte, buttons: Byte) {
        val device = targetDevice ?: return
        val report = byteArrayOf(x, y, buttons)
        hidDevice?.sendReport(device, 4, report)
    }

    @SuppressLint("MissingPermission")
    fun cleanup() {
        try {
            hidDevice?.unregisterApp()
        } catch (_: Exception) {}
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    }

    companion object {
        private val REPORT_DESCRIPTOR = byteArrayOf(
            0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
            0x09.toByte(), 0x05.toByte(), // USAGE (Game Pad)
            0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
            0x85.toByte(), 0x04.toByte(), //   REPORT_ID (4)
            0x09.toByte(), 0x30.toByte(), //   USAGE (X)
            0x09.toByte(), 0x31.toByte(), //   USAGE (Y)
            0x15.toByte(), 0x81.toByte(), //   LOGICAL_MINIMUM (-127)
            0x25.toByte(), 0x7f.toByte(), //   LOGICAL_MAXIMUM (127)
            0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
            0x95.toByte(), 0x02.toByte(), //   REPORT_COUNT (2)
            0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)
            0x05.toByte(), 0x09.toByte(), //   USAGE_PAGE (Button)
            0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Button 1)
            0x29.toByte(), 0x08.toByte(), //   USAGE_MAXIMUM (Button 8)
            0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
            0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
            0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
            0x95.toByte(), 0x08.toByte(), //   REPORT_COUNT (8)
            0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs)
            0xc0.toByte()                // END_COLLECTION
        )
    }
}
