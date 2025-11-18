package com.cs407.lab09

import kotlin.math.abs

/**
 * Represents a ball that can move. (No Android UI imports!)
 *
 * Constructor parameters:
 * - backgroundWidth: the width of the background, of type Float
 * - backgroundHeight: the height of the background, of type Float
 * - ballSize: the width/height of the ball, of type Float
 */
class Ball(
    private val backgroundWidth: Float,
    private val backgroundHeight: Float,
    private val ballSize: Float
) {
    var posX = 0f
    var posY = 0f
    var velocityX = 0f
    var velocityY = 0f
    private var accX = 0f
    private var accY = 0f

    private var isFirstUpdate = true

    init {
        // Call reset()
        reset()
    }

    /**
     * Updates the ball's position and velocity based on the given acceleration and time step.
     * (See lab handout for physics equations)
     */
    fun updatePositionAndVelocity(xAcc: Float, yAcc: Float, dT: Float) {
        if (isFirstUpdate) {
            isFirstUpdate = false
            accX = xAcc
            accY = yAcc
            return
        }

        // EQUATION 2: Calculate distance traveled
        val distX = velocityX * dT + (dT * dT / 6f) * (3 * accX + xAcc)
        val distY = velocityY * dT + (dT * dT / 6f) * (3 * accY + yAcc)

        // Update Position
        posX += distX
        posY += distY

        // EQUATION 1: Calculate new velocity
        velocityX += 0.5f * (accX + xAcc) * dT
        velocityY += 0.5f * (accY + yAcc) * dT

        // --- FRICTION & DAMPING LOGIC ---
        // 1. Apply friction to slow the ball down over time (simulating rolling resistance)
        val friction = 0.95f
        velocityX *= friction
        velocityY *= friction

        // 2. Stop the ball completely if velocity is negligible (prevents "micro-sliding")
        if (abs(velocityX) < 1f) velocityX = 0f
        if (abs(velocityY) < 1f) velocityY = 0f
        // -------------------------------

        // Update acceleration for next frame
        accX = xAcc
        accY = yAcc

        // Ensure boundaries
        checkBoundaries()
    }

    /**
     * Ensures the ball does not move outside the boundaries.
     * When it collides, velocity and acceleration perpendicular to the
     * boundary should be set to 0.
     */
    fun checkBoundaries() {
        // Left wall
        if (posX < 0f) {
            posX = 0f
            velocityX = 0f
            accX = 0f
        }
        // Right wall
        else if (posX + ballSize > backgroundWidth) {
            posX = backgroundWidth - ballSize
            velocityX = 0f
            accX = 0f
        }

        // Top wall
        if (posY < 0f) {
            posY = 0f
            velocityY = 0f
            accY = 0f
        }
        // Bottom wall
        else if (posY + ballSize > backgroundHeight) {
            posY = backgroundHeight - ballSize
            velocityY = 0f
            accY = 0f
        }
    }

    /**
     * Resets the ball to the center of the screen with zero
     * velocity and acceleration.
     */
    fun reset() {
        posX = (backgroundWidth - ballSize) / 2f
        posY = (backgroundHeight - ballSize) / 2f
        velocityX = 0f
        velocityY = 0f
        accX = 0f
        accY = 0f
        isFirstUpdate = true
    }
}