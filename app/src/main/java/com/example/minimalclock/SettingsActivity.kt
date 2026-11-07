package com.example.minimalclock

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*

class SettingsActivity : Activity() {
    private val prefs by lazy {
        getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
    }

    private val page = Color.rgb(12, 12, 14)
    private val primary = Color.WHITE
    private val secondary = Color.rgb(170, 170, 176)
    private val accent = Color.rgb(220, 220, 225)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = page
        window.navigationBarColor = page
        window.decorView.systemUiVisibility = 0

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(page)
            setPadding(dp(20), dp(18), dp(20), dp(28))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(page)
            addView(content)
        }

        fun label(text: String, size: Float = 14f, color: Int = secondary) {
            content.addView(TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(color)
                setPadding(0, dp(12), 0, dp(6))
            })
        }

        fun section(text: String) {
            content.addView(TextView(this).apply {
                this.text = text.uppercase()
                textSize = 11f
                setTextColor(secondary)
                letterSpacing = 0.12f
                setPadding(0, dp(22), 0, dp(8))
            })
        }

        fun addSizeSetting(title: String, key: String, default: Int, min: Int, max: Int) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val name = TextView(this).apply {
                text = title
                textSize = 14f
                setTextColor(primary)
            }
            val value = TextView(this).apply {
                textSize = 12f
                setTextColor(secondary)
                gravity = android.view.Gravity.END
                minWidth = dp(38)
            }
            row.addView(name, LinearLayout.LayoutParams(0, dp(32), 1f))
            row.addView(value)
            content.addView(row)

            val seek = SeekBar(this).apply {
                this.max = max - min
                progress = prefs.getInt(key, default).coerceIn(min, max) - min
                progressTintList = android.content.res.ColorStateList.valueOf(accent)
                thumbTintList = android.content.res.ColorStateList.valueOf(primary)
                progressBackgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.DKGRAY)
            }
            fun showValue(v: Int) { value.text = v.toString() }
            showValue(seek.progress + min)
            content.addView(seek, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)
            ))
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val actual = progress + min
                    showValue(actual)
                    if (fromUser) {
                        prefs.edit().putInt(key, actual).apply()
                        ClockWidgetProvider.updateAll(this@SettingsActivity)
                    }
                }
                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) {}
            })
        }

        fun addSpinner(title: String, options: List<String>, selected: Int, onSelected: (Int) -> Unit) {
            label(title, 14f, primary)
            val spinner = Spinner(this).apply {
                adapter = ArrayAdapter(
                    this@SettingsActivity,
                    android.R.layout.simple_spinner_item,
                    options
                ).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                setSelection(selected.coerceIn(options.indices))
                backgroundTintList =
                    android.content.res.ColorStateList.valueOf(secondary)
            }
            content.addView(spinner, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)
            ))
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                private var first = true
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (first) { first = false; return }
                    onSelected(position)
                }
            }
        }

        content.addView(TextView(this).apply {
            text = "MINIMAL CLOCK"
            textSize = 20f
            setTextColor(primary)
            letterSpacing = 0.04f
            setPadding(0, dp(4), 0, dp(2))
        })
        label("Personalise your home-screen widget", 13f, secondary)

        section("Typography")
        addSizeSetting("Weekday size", "day_size", 34, 10, 100)
        addSizeSetting("Time size", "time_size", 58, 10, 100)
        addSizeSetting("Date size", "date_size", 22, 10, 100)

        val weights = listOf("Thin", "Light", "Regular", "Medium", "Bold")
        addSpinner("Time font weight", weights, prefs.getInt("weight", 0).coerceIn(0, 4)) {
            position ->
            prefs.edit().putInt("weight", position).apply()
            ClockWidgetProvider.updateAll(this)
        }

        section("Colours")
        val colors = linkedMapOf(
            "White" to Color.WHITE,
            "Soft white" to Color.rgb(225, 225, 225),
            "Red" to Color.RED,
            "Blue" to Color.BLUE,
            "Yellow" to Color.YELLOW,
            "Green" to Color.GREEN
        )
        val currentColor = prefs.getInt("foreground", Color.WHITE)
        val colorIndex = colors.values.indexOf(currentColor).coerceAtLeast(0)
        addSpinner("Text color", colors.keys.toList(), colorIndex) { position ->
            prefs.edit().putInt("foreground", colors.values.toList()[position]).apply()
            ClockWidgetProvider.updateAll(this)
        }

        val backgrounds = linkedMapOf(
            "Transparent" to Color.TRANSPARENT,
            "Black" to Color.BLACK,
            "Dark charcoal" to Color.rgb(30, 30, 30),
            "White" to Color.WHITE,
            "Red" to Color.RED,
            "Blue" to Color.BLUE
        )
        val currentBackground = prefs.getInt("background", Color.TRANSPARENT)
        val backgroundIndex = backgrounds.values.indexOf(currentBackground).coerceAtLeast(0)
        addSpinner("Widget background", backgrounds.keys.toList(), backgroundIndex) { position ->
            prefs.edit().putInt("background", backgrounds.values.toList()[position]).apply()
            ClockWidgetProvider.updateAll(this)
        }

        label("Changes are applied to your widget automatically.", 12f, secondary)
        setContentView(scroll)
    }
}
