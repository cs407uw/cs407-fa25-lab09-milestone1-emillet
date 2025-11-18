package com.cs407.lab09

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class BallViewModel : ViewModel() {

    private var ball: Ball? = null
    private var lastTimestamp: Long = 0L

    // Expose the ball's position as a StateFlow
    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            ball = Ball(fieldWidth, fieldHeight, ballSizePx)
            ball?.let { b ->
                _ballPosition.value = Offset(b.posX, b.posY)
            }
        }
    }

    fun onSensorDataChanged(event: SensorEvent) {
        val currentBall = ball ?: return

        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            if (lastTimestamp != 0L) {
                val NS2S = 1.0f / 1_000_000_000.0f
                val dT = (event.timestamp - lastTimestamp) * NS2S

                // --- TUNING CONSTANTS ---
                val ACC_SENSITIVITY = 500f // Increased slightly for better speed
                val SENSOR_THRESHOLD = 0.2f

                // X AXIS:
                // Screen X is Right(+). Sensor X (Tilt Left) is Positive.
                // We need to invert Sensor X so tilting left moves left (-).
                var rawX = -event.values[0]

                // Y AXIS:
                // Screen Y is Down(+). Sensor Y (Tilt Bottom-Down) is Positive.
                // We keep this POSITIVE so tilting the bottom down increases Y.
                // (This fixes the "Top Left" vs "Bottom Left" issue)
                var rawY = event.values[1]

                // Apply Threshold to stop "drift" when flat
                if (abs(rawX) < SENSOR_THRESHOLD) rawX = 0f
                if (abs(rawY) < SENSOR_THRESHOLD) rawY = 0f

                // Scale Values
                val xAcc = rawX * ACC_SENSITIVITY
                val yAcc = rawY * ACC_SENSITIVITY

                currentBall.updatePositionAndVelocity(xAcc, yAcc, dT)

                _ballPosition.update { Offset(currentBall.posX, currentBall.posY) }
            }
            lastTimestamp = event.timestamp
        }
    }

    fun reset() {
        // 1. Reset the physics object
        ball?.reset()

        // 2. Force the UI to Center immediately
        ball?.let { b ->
            _ballPosition.value = Offset(b.posX, b.posY)
        }

        // 3. Reset time so we don't calculate a huge jump in the next frame
        lastTimestamp = 0L
    }
}