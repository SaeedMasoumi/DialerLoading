package io.saeid.dialerloading

import android.view.animation.Interpolator
import kotlin.math.E
import kotlin.math.cos
import kotlin.math.pow

/**
 * @author Saeed Masoumi (7masoumi@gmail.com)
 */
internal class SpringInterpolator constructor(private val damping: Float,
        private val velocity: Float) : Interpolator {

    private val fromValue = 0f
    private val toValue = 1f

    override fun getInterpolation(x: Float): Float {
        val distance = toValue - fromValue
        val normalizedValue = normalizeFunction(x, damping, velocity)
        return toValue - distance * normalizedValue
    }

    private fun normalizeFunction(x: Float, damping: Float, velocity: Float) = (
            E.pow((-damping * x * 10.0)) *
                    cos((velocity * x * 10.0))
            ).toFloat()
}