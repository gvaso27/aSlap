package com.beetle.aslap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.beetle.aslap.ui.SlapScreen
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

    private val slapEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val slapReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            slapEvents.tryEmit(Unit)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request runtime Notification permission for Android 13+ (Required for Foreground Services)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // Fire up the background monitor service
        val serviceIntent = Intent(this, SlapService::class.java)
        startForegroundService(serviceIntent)

        setContent {
            SlapScreen(
                slapEvents = slapEvents,
                onManualTap = {
                    // Send an intent directly to our own service to execute a simulated physical slap
                    slapEvents.tryEmit(Unit)
                    val triggerIntent = Intent("COM.BEETLE.ASLAP.SLAP_TRIGGERED").apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(triggerIntent)

                    // Actually run the sound/vibe via service manually
                    // To do this simply, we can call the service or let the service handle it.
                    // For UI taps, we can safely simulate a hardware strike event:
                    val strikeIntent = Intent(this@MainActivity, SlapService::class.java)
                    // Our service setup triggers logic instantly on cycle start
                    startService(strikeIntent)
                },
                onStopService = {
                    val stopIntent = Intent(this@MainActivity, SlapService::class.java).apply {
                        action = "STOP_ACTION"
                    }
                    startService(stopIntent)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("COM.BEETLE.ASLAP.SLAP_TRIGGERED")
        ContextCompat.registerReceiver(
            this,
            slapReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(slapReceiver)
    }
}
