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

                val ACC_SENSITIVITY = 500f
                val SENSOR_THRESHOLD = 0.2f

                var rawX = -event.values[0]

                var rawY = event.values[1]

                if (abs(rawX) < SENSOR_THRESHOLD) rawX = 0f
                if (abs(rawY) < SENSOR_THRESHOLD) rawY = 0f

                val xAcc = rawX * ACC_SENSITIVITY
                val yAcc = rawY * ACC_SENSITIVITY

                currentBall.updatePositionAndVelocity(xAcc, yAcc, dT)

                _ballPosition.update { Offset(currentBall.posX, currentBall.posY) }
            }
            lastTimestamp = event.timestamp
        }
    }

    fun reset() {
        ball?.reset()

        ball?.let { b ->
            _ballPosition.value = Offset(b.posX, b.posY)
        }

        lastTimestamp = 0L
    }
}