package io.saeid.dialerloading.sample

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.dialing_tv
import kotlinx.android.synthetic.main.activity_main.rotary_dialer


class DialerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addGradientToTV()
        Handler().postDelayed({
            rotary_dialer.dial(digits = intArrayOf(1, 3, 5))
        }, 1500)
    }

    private fun addGradientToTV() {
        dialing_tv.apply {
            measure(0, 0)
            paint.apply {
                shader = LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f,
                        Color.parseColor("#bff76f"), Color.parseColor("#9be6a2"), TileMode.CLAMP)
            }
        }
    }
}
