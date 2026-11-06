package com.example.minimalclock

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.content.Context

class SettingsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.BLACK)
        }

        fun addLabel(text: String) {
            layout.addView(TextView(this).apply {
                this.text = text
                textSize = 18f
                setTextColor(Color.WHITE)
                setPadding(0, 18, 0, 8)
            })
        }

        fun addSizeSetting(label: String, key: String, default: Int) {
            addLabel("$label size")
            val seek = SeekBar(this).apply {
                max = 100
                progress = prefs.getInt(key, default).coerceIn(10, 100)
            }
            layout.addView(seek)
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                    prefs.edit().putInt(key, p.coerceAtLeast(10)).apply()
                    ClockWidgetProvider.updateAll(this@SettingsActivity)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        addLabel("MINIMAL CLOCK SETTINGS")

        addSizeSetting("Weekday", "day_size", 34)
        addSizeSetting("Time", "time_size", 58)
        addSizeSetting("Date", "date_size", 22)

        addLabel("Font weight")
        val weights = listOf("Thin", "Light", "Regular", "Medium", "Bold")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            weights
        )
        spinner.setSelection(prefs.getInt("weight", 0))
        layout.addView(spinner)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                prefs.edit().putInt("weight", position).apply()
                ClockWidgetProvider.updateAll(this@SettingsActivity)
            }
        }

        addLabel("Text color")
        val colors = linkedMapOf(
            "White" to Color.WHITE,
            "Black" to Color.BLACK,
            "Red" to Color.RED,
            "Blue" to Color.BLUE,
            "Yellow" to Color.YELLOW,
            "Green" to Color.GREEN
        )
        val colorSpinner = Spinner(this)
        colorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            colors.keys.toList()
        )
        colorSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                prefs.edit().putInt("foreground", colors.values.toList()[position]).apply()
                ClockWidgetProvider.updateAll(this@SettingsActivity)
            }
        }
        layout.addView(colorSpinner)

        addLabel("Background")
        val backgrounds = linkedMapOf(
            "Transparent" to Color.TRANSPARENT,
            "Black" to Color.BLACK,
            "White" to Color.WHITE,
            "Red" to Color.RED,
            "Blue" to Color.BLUE
        )
        val bgSpinner = Spinner(this)
        bgSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            backgrounds.keys.toList()
        )
        bgSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                prefs.edit().putInt("background", backgrounds.values.toList()[position]).apply()
                ClockWidgetProvider.updateAll(this@SettingsActivity)
            }
        }
        layout.addView(bgSpinner)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }
}
