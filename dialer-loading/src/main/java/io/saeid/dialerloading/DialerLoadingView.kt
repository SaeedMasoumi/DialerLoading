package io.saeid.dialerloading

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.ofFloat
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.Color.parseColor
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Path
import android.graphics.Path.Direction.CCW
import android.graphics.PorterDuff.Mode.CLEAR
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import java.lang.Math.toRadians
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.properties.Delegates


/**
 * A rotary dialer loading view.
 *
 * @author Saeed Masoumi (7masoumi@gmail.com)
 */
class DialerLoadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        const val DEFAULT_DIALER_DIGIT_COLOR = Color.WHITE
        const val DEFAULT_TRIANGLE_COLOR = Color.WHITE
        const val NUMBER_OF_HOLES = 12
        const val HOLE_ANGLE = 360F / NUMBER_OF_HOLES
        const val DEFAULT_DIALER_DURATION = 2000

        val DEFAULT_DIALER_COLOR_START = Color.parseColor("#c8f587")
        val DROP_SHADOW_COLOR = Color.parseColor("#80000000")
        val DEFAULT_DIALER_BG_COLOR = parseColor("#B6B6B6")
        val DEFAULT_DIALER_COLOR_END = Color.parseColor("#9be6a2")
        //just to hiding the overlapped pixels between background and dialer for smoother appearance
        val BACKGROUND_CIRCLE_OFFSET = 4.dp
        val IGNORED_HOLES = setOf(1, 2)
    }

    // timing
    private var dialDuration by Delegates.notNull<Long>()
    // Colors
    private var dialerBackgroundColor by Delegates.notNull<Int>()
    private var dialerColorStart by Delegates.notNull<Int>()
    private var dialerColorEnd by Delegates.notNull<Int>()
    private var dialerDigitColor by Delegates.notNull<Int>()
    private var triangleColor by Delegates.notNull<Int>()

    // Paints
    private val backgroundPaint = Paint()
    private val dialerHolePaint = Paint()
    private val dialerDigitPaint = Paint()
    private val dialerPaint = Paint()
    private val trianglePaint = Paint()
    // Positions
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var holeRadius: Float = 0f
    private var holeRadiusWithOffset: Float = 0f
    private var innerRadius: Float = 0f
    // Bitmaps
    private var bitmap: Bitmap? = null
    private var dialerMatrix = Matrix()
    // Rotation
    private var dialerRotation = 0f
    // Animator
    private var animatorSet: AnimatorSet? = null
    // Path
    private var trianglePath = Path()

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DialerLoadingView,
                0, 0).apply {
            try {
                dialerBackgroundColor = getColor(
                        R.styleable.DialerLoadingView_dialer_background_color,
                        DEFAULT_DIALER_BG_COLOR)
                dialerColorStart = getColor(
                        R.styleable.DialerLoadingView_dialer_color_start,
                        DEFAULT_DIALER_COLOR_START)
                dialerColorEnd = getColor(
                        R.styleable.DialerLoadingView_dialer_color_end,
                        DEFAULT_DIALER_COLOR_END)
                dialerDigitColor = getColor(
                        R.styleable.DialerLoadingView_dialer_digit_color,
                        DEFAULT_DIALER_DIGIT_COLOR)
                dialDuration = getInt(R.styleable.DialerLoadingView_dialer_duration,
                        DEFAULT_DIALER_DURATION).toLong()
                triangleColor = getColor(
                        R.styleable.DialerLoadingView_dialer_triangle_color,
                        DEFAULT_TRIANGLE_COLOR)
            } finally {
                recycle()
            }
        }
        setPaints()
    }

    private fun setPaints() {
        backgroundPaint.apply {
            color = dialerBackgroundColor
            isAntiAlias = true
        }
        dialerHolePaint.apply {
            style = FILL
            xfermode = PorterDuffXfermode(CLEAR)
            isAntiAlias = true
        }
        dialerDigitPaint.apply {
            color = dialerDigitColor
            isAntiAlias = true
        }
        trianglePaint.apply {
            style = FILL
            color = triangleColor
            isAntiAlias = true
            pathEffect = CornerPathEffect(12.dp.toFloat())
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePositions()
        updateTriangle()
        applyShadow()
        // create dialer bitmap
        bitmap?.recycle()
        bitmap = createDialerBitmap()
    }

    private fun updatePositions() {
        centerX = width / 2f
        centerY = height / 2f
        radius = min(width, height) / 2f - BACKGROUND_CIRCLE_OFFSET
        holeRadius = (PI * radius / NUMBER_OF_HOLES).toFloat()
        holeRadiusWithOffset = holeRadius / 3f
        innerRadius = radius - holeRadius // radius of a circle which crossing from the center of each hole
    }

    private fun updateTriangle() {
        trianglePath = Path()
        //bottom right
        trianglePath.moveTo(width.toFloat(), centerY)
        val leftX = centerX + innerRadius - holeRadius * 2
        //left top
        trianglePath.lineTo(leftX, centerY)
        val outerY = outerCircleY(0.0).toFloat() + holeRadius
        //top right
        trianglePath.lineTo(width.toFloat(), outerY)
        trianglePath.close()
    }

    private fun applyShadow() {
        // add drop shadow to every digits
        dialerDigitPaint.setShadowLayer(holeRadiusWithOffset / 6f, 0f, 0f, dialerDigitColor)
        val radius = outerCircleY(0.0).toFloat() + holeRadius - centerY
        trianglePaint.setShadowLayer(radius / 6f, 0f, 0f, DROP_SHADOW_COLOR)
        setLayerType(LAYER_TYPE_SOFTWARE, dialerDigitPaint)
    }

    private fun createDialerBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val path = Path().apply {
            addCircle(centerX, centerY, radius, CCW)
        }
        bitmap.eraseColor(TRANSPARENT)
        val canvas = Canvas(bitmap)
        canvas.clipPath(path)
        applyGradientShader()
        canvas.drawPath(path, dialerPaint)
        createDialerHoles(canvas)
        return bitmap
    }


    private fun applyGradientShader() {
        dialerPaint.shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                dialerColorStart, dialerColorEnd,
                Shader.TileMode.CLAMP)
    }

    private fun createDialerHoles(canvas: Canvas) {
        for (h in 0 until NUMBER_OF_HOLES) {
            if (IGNORED_HOLES.contains(h + 1)) continue
            val rad = toRadians(((h * HOLE_ANGLE).toDouble()))
            val px = innerCircleX(rad).toFloat()
            val py = innerCircleY(rad).toFloat()
            canvas.drawCircle(px, py, holeRadiusWithOffset, dialerHolePaint)
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //draw background
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
        //draw digits on background
        drawDigitHoles(canvas)
        //draw dialer bitmap
        bitmap?.let {
            canvas.drawBitmap(it, dialerMatrix.rotate(), null)
        }
        //draw triangle on the right side of the dialr
        drawTriangleBoundary(canvas)
    }

    private fun drawDigitHoles(canvas: Canvas) {
        for (h in 0 until NUMBER_OF_HOLES) {
            if (IGNORED_HOLES.contains(h + 1)) continue
            val rad = toRadians(((h * HOLE_ANGLE).toDouble()))
            val px = innerCircleX(rad).toFloat()
            val py = innerCircleY(rad).toFloat()
            canvas.drawCircle(px, py, holeRadiusWithOffset / 3,
                    dialerDigitPaint)
        }
    }

    private fun drawTriangleBoundary(canvas: Canvas) {
        canvas.save()
        canvas.rotate(HOLE_ANGLE / 2f, centerX, centerY)
        canvas.save()
        canvas.drawPath(trianglePath, trianglePaint)
        canvas.restore()
        canvas.restore()
    }

    private fun Matrix.rotate(): Matrix {
        setRotate(dialerRotation, centerX, centerY)
        return this
    }

    fun dial(endless: Boolean = true, digits: IntArray) {
        animatorSet?.let {
            if (it.isRunning) {
                return
            } else {
                it.clear()
            }
        }
        val valueAnimators = mutableListOf<ValueAnimator>()
        digits.forEach {
            val angle = it * HOLE_ANGLE
            valueAnimators.add(animator(0f, angle))
            valueAnimators.add(animator(angle, 0f))
        }
        animatorSet = AnimatorSet().apply {
            playSequentially(valueAnimators.toList())
            if (endless) {
                setEndlessRepeat()
            }
            start()
        }
    }

    fun hangUp() {
        animatorSet?.removeAllListeners()
        animatorSet?.cancel()
    }

    private fun animator(from: Float, to: Float) = ofFloat(from, to).apply {
        interpolator = SpringInterpolator(1f, 0f)
        duration = dialDuration
        addUpdateListener {
            dialerRotation = it.animatedValue as Float
            invalidate()
        }
    }

    private fun innerCircleX(rad: Double) = centerX + (innerRadius) * cos(rad)
    private fun innerCircleY(rad: Double) = centerY + (innerRadius) * sin(rad)
    private fun outerCircleX(rad: Double) = centerX + (radius) * cos(rad)
    private fun outerCircleY(rad: Double) = centerY + (radius) * sin(rad)

}

private fun AnimatorSet.setEndlessRepeat() {
    addListener(object : AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationEnd(animation: Animator?) {
            start()
        }

        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}

    })
}

private fun AnimatorSet.clear() {
    childAnimations.forEach { it.removeAllListeners() }
    removeAllListeners()
}


private val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
