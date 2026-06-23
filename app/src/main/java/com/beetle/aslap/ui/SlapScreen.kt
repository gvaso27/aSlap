package com.beetle.aslap.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SlapScreen(
    slapEvents: SharedFlow<Unit>,
    onManualTap: () -> Unit,
    onStopService: () -> Unit
) {
    var rippleTrigger by remember { mutableStateOf(0) }

    // Listen for the broadcast event from background hardware triggers
    LaunchedEffect(Unit) {
        slapEvents.collectLatest {
            rippleTrigger++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14)), // Deep dark chaotic canvas background
        contentAlignment = Alignment.Center
    ) {
        // Visualizing the shockwaves (Render up to 3 historic overlapping ripples)
        repeat(3) { index ->
            val delay = index * 150
            RippleWave(triggerCount = rippleTrigger, delayMillis = delay)
        }

        // Central interaction system
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "iSlap",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The app that rewards bad behavior.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(64.dp))

            // The main interactive Target
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFFE91E63), shape = CircleShape) // Hot Pink 🍑 vibe
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onManualTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🍑", fontSize = 80.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStopService,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                Text("Stop Service")
            }
        }
    }
}

@Composable
fun RippleWave(triggerCount: Int, delayMillis: Int) {
    if (triggerCount == 0) return

    val animatableRadius = remember { Animatable(0f) }
    val animatableAlpha = remember { Animatable(1f) }

    LaunchedEffect(triggerCount) {
        animatableRadius.snapTo(0f)
        animatableAlpha.snapTo(1f)

        animatableRadius.animateTo(
            targetValue = 400f,
            animationSpec = transitionspec(delayMillis)
        )
    }

    LaunchedEffect(triggerCount) {
        animatableAlpha.snapTo(1f)
        animatableAlpha.animateTo(
            targetValue = 0f,
            animationSpec = transitionspec(delayMillis)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFE91E63).copy(alpha = animatableAlpha.value),
            radius = animatableRadius.value,
            style = Stroke(width = 6f)
        )
    }
}

private fun transitionspec(delay: Int): TweenSpec<Float> = tween(
    durationMillis = 800,
    delayMillis = delay,
    easing = LinearOutSlowInEasing
)