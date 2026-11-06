
package com.example.minimalclock

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.content.Context
import android.view.View
import android.widget.*

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

            seek.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            prefs.edit()
                                .putInt(key, progress.coerceAtLeast(10))
                                .apply()
                            ClockWidgetProvider.updateAll(this@SettingsActivity)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
            )
        }

        fun addSpinner(
            label: String,
            options: List<String>,
            selectedIndex: Int,
            onSelected: (Int) -> Unit
        ) {
            addLabel(label)

            val spinner = Spinner(this)
            spinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
            )

            spinner.setSelection(selectedIndex.coerceIn(options.indices))

            spinner.onItemSelectedListener =
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(
                        parent: android.widget.AdapterView<*>?
                    ) {}

                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        onSelected(position)
                    }
                }

            layout.addView(spinner)
        }

        addLabel("MINIMAL CLOCK SETTINGS")

        addSizeSetting("Weekday", "day_size", 34)
        addSizeSetting("Time", "time_size", 58)
        addSizeSetting("Date", "date_size", 22)

        val weights = listOf("Thin", "Light", "Regular", "Medium", "Bold")

        addSpinner(
            "Time font weight",
            weights,
            prefs.getInt("weight", 0).coerceIn(0, 4)
        ) { position ->
            prefs.edit().putInt("weight", position).apply()
            ClockWidgetProvider.updateAll(this)
        }

        val colors = linkedMapOf(
            "White" to Color.WHITE,
            "Black" to Color.BLACK,
            "Red" to Color.RED,
            "Blue" to Color.BLUE,
            "Yellow" to Color.YELLOW,
            "Green" to Color.GREEN
        )

        val currentColor = prefs.getInt("foreground", Color.WHITE)
        val colorIndex = colors.values.indexOf(currentColor).coerceAtLeast(0)

        addSpinner(
            "Text color",
            colors.keys.toList(),
            colorIndex
        ) { position ->
            prefs.edit()
                .putInt("foreground", colors.values.toList()[position])
                .apply()
            ClockWidgetProvider.updateAll(this)
        }

        val backgrounds = linkedMapOf(
            "Transparent" to Color.TRANSPARENT,
            "Black" to Color.BLACK,
            "White" to Color.WHITE,
            "Red" to Color.RED,
            "Blue" to Color.BLUE
        )

        val currentBackground = prefs.getInt("background", Color.TRANSPARENT)
        val backgroundIndex =
            backgrounds.values.indexOf(currentBackground).coerceAtLeast(0)

        addSpinner(
            "Widget background",
            backgrounds.keys.toList(),
            backgroundIndex
        ) { position ->
            prefs.edit()
                .putInt("background", backgrounds.values.toList()[position])
                .apply()
            ClockWidgetProvider.updateAll(this)
        }

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }
}
