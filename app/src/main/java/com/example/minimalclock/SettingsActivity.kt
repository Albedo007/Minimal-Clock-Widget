package com.example.minimalclock

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
    }

    private val page = Color.rgb(10, 10, 12)
    private val panel = Color.rgb(24, 24, 28)
    private val panelSoft = Color.rgb(31, 31, 36)
    private val white = Color.rgb(247, 247, 249)
    private val muted = Color.rgb(160, 160, 170)
    private val accent = Color.rgb(255, 48, 72)

    private lateinit var previewSurface: LinearLayout
    private lateinit var previewWeekday: TextView
    private lateinit var previewTime: TextView
    private lateinit var previewDate: TextView
    private lateinit var previewBackgroundNote: TextView

    private data class Choice(val label: String, val color: Int)

    private val textColors = listOf(
        Choice("White", Color.WHITE),
        Choice("Soft", Color.rgb(225, 225, 230)),
        Choice("Red", Color.rgb(255, 65, 85)),
        Choice("Blue", Color.rgb(105, 170, 255)),
        Choice("Yellow", Color.rgb(255, 220, 100)),
        Choice("Green", Color.rgb(115, 220, 160))
    )

    private val backgrounds = listOf(
        Choice("Transparent", Color.TRANSPARENT),
        Choice("Black", Color.BLACK),
        Choice("Charcoal", Color.rgb(30, 30, 34)),
        Choice("White", Color.WHITE),
        Choice("Red", Color.rgb(150, 20, 38)),
        Choice("Blue", Color.rgb(20, 55, 110))
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun rounded(
        color: Int,
        radius: Int,
        stroke: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) {
                setStroke(dp(1), stroke)
            }
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = white,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            typeface = if (bold)
                Typeface.create("sans-serif", Typeface.BOLD)
            else
                Typeface.create("sans-serif", Typeface.NORMAL)
            includeFontPadding = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = page
        window.navigationBarColor = page
        window.decorView.systemUiVisibility = 0

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(32))
            setBackgroundColor(page)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(page)
            addView(content)
        }

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val headingColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        headingColumn.addView(
            text("MINIMAL CLOCK", 21f, white, true)
        )

        val subtitle = text(
            "Make time look yours.",
            13f,
            muted
        )

        subtitle.setPadding(0, dp(6), 0, 0)
        headingColumn.addView(subtitle)
        header.addView(headingColumn)

        val live = text("● LIVE", 11f, accent, true).apply {
            setPadding(dp(10), dp(7), dp(10), dp(7))
            background = rounded(
                Color.rgb(48, 19, 26), 30
            )
        }

        header.addView(live)
        content.addView(header)

        // LIVE PREVIEW CARD
        val previewCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(
                panel, 24, Color.rgb(48, 48, 55)
            )
        }

        val previewTop = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        previewTop.addView(
            text("LIVE PREVIEW", 11f, muted, true),
            LinearLayout.LayoutParams(0, dp(20), 1f)
        )

        previewTop.addView(
            text("Updates as you edit", 11f, muted)
        )

        previewCard.addView(previewTop)

        previewSurface = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            minimumHeight = dp(190)
            setPadding(dp(12), dp(22), dp(12), dp(22))
            background = rounded(
                panelSoft, 18, Color.rgb(65, 65, 72)
            )
        }

        val previewColor = prefs.getInt(
            "foreground", Color.WHITE
        )

        previewWeekday = text(
            "SATURDAY", 13f, previewColor
        ).apply {
            letterSpacing = 0.20f
            gravity = Gravity.CENTER
        }

        previewTime = text(
            "09:41", 58f, previewColor
        ).apply {
            gravity = Gravity.CENTER
            setSingleLine(true)
        }

        previewDate = text(
            "NOVEMBER 07", 12f, previewColor
        ).apply {
            letterSpacing = 0.14f
            gravity = Gravity.CENTER
        }

        previewSurface.addView(previewWeekday)

        previewSurface.addView(
            previewTime,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
                bottomMargin = dp(8)
            }
        )

        previewSurface.addView(previewDate)

        previewCard.addView(
            previewSurface,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        previewBackgroundNote = text(
            "TRANSPARENT BACKGROUND", 10f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }

        previewCard.addView(previewBackgroundNote)

        content.addView(
            previewCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        refreshPreview()

        // TYPOGRAPHY
        addSection(
            content,
            "TYPOGRAPHY",
            "Fine-tune every line"
        )

        val typographyCard = newCard()

        addSlider(
            typographyCard,
            "Weekday size",
            "day_size",
            34, 10, 100, "sp"
        )

        addDivider(typographyCard)

        addSlider(
            typographyCard,
            "Time size",
            "time_size",
            58, 10, 100, "sp"
        )

        addDivider(typographyCard)

        addSlider(
            typographyCard,
            "Date size",
            "date_size",
            22, 10, 100, "sp"
        )

        content.addView(typographyCard)

        // FONT WEIGHT
        addSection(
            content,
            "TIME WEIGHT",
            "Choose the feel of your clock"
        )

        val weightCard = newCard()

        val weightNames = listOf(
            "Thin",
            "Light",
            "Regular",
            "Medium",
            "Bold"
        )

        val weightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        weightNames.forEachIndexed { index, name ->

            val chip = text(name, 11f, white).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(12), dp(8), dp(12))
                background = rounded(panelSoft, 12)
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    prefs.edit()
                        .putInt("weight", index)
                        .apply()

                    refreshPreview()

                    ClockWidgetProvider.updateAll(
                        this@SettingsActivity
                    )

                    updateWeightSelection(
                        weightRow,
                        weightNames,
                        index
                    )
                }

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    dp(44),
                    1f
                ).apply {
                    marginEnd = dp(5)
                }
            }

            weightRow.addView(chip)
        }

        weightCard.addView(weightRow)
        content.addView(weightCard)

        updateWeightSelection(
            weightRow,
            weightNames,
            prefs.getInt("weight", 0).coerceIn(0, 4)
        )

        // TEXT COLOR
        addSection(
            content,
            "TEXT COLOUR",
            "Pick a colour for the clock"
        )

        val textColorCard = newCard()

        addColorChoices(
            textColorCard,
            textColors,
            "foreground"
        )

        content.addView(textColorCard)

        // BACKGROUND
        addSection(
            content,
            "WIDGET BACKGROUND",
            "Transparent or a solid colour"
        )

        val backgroundCard = newCard()

        addColorChoices(
            backgroundCard,
            backgrounds,
            "background"
        )

        content.addView(backgroundCard)

        // FOOTER
        val footer = text(
            "Changes save automatically and update your home-screen widget.",
            12f,
            muted
        )

        footer.gravity = Gravity.CENTER
        footer.setPadding(
            dp(12), dp(22), dp(12), dp(8)
        )

        content.addView(footer)

        setContentView(scroll)
    }

    private fun newCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = rounded(
                panel, 20, Color.rgb(42, 42, 48)
            )
        }
    }

    private fun addSection(
        parent: LinearLayout,
        title: String,
        subtitle: String
    ) {
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(24), 0, dp(10))
        }

        block.addView(
            text(title, 11f, accent, true)
        )

        val sub = text(subtitle, 12f, muted)
        sub.setPadding(0, dp(5), 0, 0)

        block.addView(sub)
        parent.addView(block)
    }

    private fun addDivider(parent: LinearLayout) {
        val line = View(this).apply {
            setBackgroundColor(Color.rgb(48, 48, 54))
        }

        parent.addView(
            line,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )
    }

    private fun addSlider(
        parent: LinearLayout,
        title: String,
        key: String,
        default: Int,
        min: Int,
        max: Int,
        suffix: String
    ) {
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        top.addView(
            text(title, 14f, white),
            LinearLayout.LayoutParams(0, dp(28), 1f)
        )

        val value = text("", 12f, accent, true).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }

        top.addView(value)
        block.addView(top)

        val seek = SeekBar(this).apply {
            this.max = max - min

            progress = prefs.getInt(key, default)
                .coerceIn(min, max) - min

            progressTintList =
                android.content.res.ColorStateList.valueOf(accent)

            thumbTintList =
                android.content.res.ColorStateList.valueOf(white)

            progressBackgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(70, 70, 78)
                )
        }

        fun show(v: Int) {
            value.text = "$v$suffix"
        }

        show(seek.progress + min)

        block.addView(
            seek,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        seek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    bar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val actual = progress + min
                    show(actual)

                    if (fromUser) {
                        prefs.edit()
                            .putInt(key, actual)
                            .apply()

                        refreshPreview()

                        ClockWidgetProvider.updateAll(
                            this@SettingsActivity
                        )
                    }
                }

                override fun onStartTrackingTouch(
                    bar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    bar: SeekBar?
                ) {}
            }
        )

        parent.addView(block)
    }

    private fun addColorChoices(
        parent: LinearLayout,
        choices: List<Choice>,
        preferenceKey: String
    ) {
        val selected = prefs.getInt(
            preferenceKey,
            if (preferenceKey == "foreground")
                Color.WHITE
            else
                Color.TRANSPARENT
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        choices.forEach { choice ->

            val selectedNow = choice.color == selected

            val chip = TextView(this).apply {

                text = if (selectedNow)
                    "✓ ${choice.label}"
                else
                    choice.label

                textSize = 11f
                gravity = Gravity.CENTER

                setTextColor(
                    if (
                        choice.color == Color.WHITE ||
                        choice.color == Color.YELLOW
                    ) Color.BLACK else white
                )

                setPadding(
                    dp(11), dp(10), dp(11), dp(10)
                )

                background = rounded(
                    if (preferenceKey == "foreground")
                        choice.color
                    else if (choice.color == Color.TRANSPARENT)
                        panelSoft
                    else
                        choice.color,
                    30,
                    if (selectedNow)
                        accent
                    else
                        Color.rgb(65, 65, 72)
                )

                isClickable = true
                isFocusable = true

                setOnClickListener {

                    prefs.edit()
                        .putInt(preferenceKey, choice.color)
                        .apply()

                    refreshPreview()

                    ClockWidgetProvider.updateAll(
                        this@SettingsActivity
                    )

                    recreate()
                }

                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(38)
                ).apply {
                    marginEnd = dp(7)
                }
            }

            row.addView(chip)
        }

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }

        parent.addView(
            horizontal,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )
    }

    private fun updateWeightSelection(
        row: LinearLayout,
        names: List<String>,
        selected: Int
    ) {
        for (i in 0 until row.childCount) {

            val view = row.getChildAt(i) as TextView

            view.background = rounded(
                if (i == selected)
                    Color.rgb(75, 20, 31)
                else
                    panelSoft,
                12,
                if (i == selected)
                    accent
                else
                    Color.rgb(65, 65, 72)
            )

            view.setTextColor(
                if (i == selected) accent else white
            )
        }
    }

    private fun refreshPreview() {

        if (!::previewTime.isInitialized) return

        val now = Date()
        val locale = Locale.getDefault()

        previewWeekday.text =
            SimpleDateFormat("EEEE", locale)
                .format(now)
                .uppercase(locale)

        previewTime.text =
            SimpleDateFormat("HH:mm", locale)
                .format(now)

        previewDate.text =
            SimpleDateFormat("MMMM dd", locale)
                .format(now)
                .uppercase(locale)

        // TEXT COLOR
        val foreground = prefs.getInt(
            "foreground", Color.WHITE
        )

        previewWeekday.setTextColor(foreground)
        previewTime.setTextColor(foreground)
        previewDate.setTextColor(foreground)

        // FONT SIZES
        previewWeekday.textSize =
            prefs.getInt("day_size", 34)
                .coerceAtLeast(10).toFloat()

        previewTime.textSize =
            prefs.getInt("time_size", 58)
                .coerceAtLeast(10).toFloat()

        previewDate.textSize =
            prefs.getInt("date_size", 22)
                .coerceAtLeast(10).toFloat()

        // FONT WEIGHT
        val weight = prefs.getInt(
            "weight", 0
        ).coerceIn(0, 4)

        val typeface = when (weight) {
            0 -> Typeface.create(
                "sans-serif-thin", Typeface.NORMAL
            )
            1 -> Typeface.create(
                "sans-serif-light", Typeface.NORMAL
            )
            2 -> Typeface.create(
                "sans-serif", Typeface.NORMAL
            )
            3 -> Typeface.create(
                "sans-serif-medium", Typeface.NORMAL
            )
            else -> Typeface.create(
                "sans-serif", Typeface.BOLD
            )
        }

        previewTime.typeface = typeface

        // WIDGET BACKGROUND
        val background = prefs.getInt(
            "background", Color.TRANSPARENT
        )
        val previewColor =
            if (background == Color.TRANSPARENT)
                panelSoft
            else
                background

        previewSurface.background = rounded(
            previewColor,
            18,
            Color.rgb(65, 65, 72)
        )

        previewBackgroundNote.text =
            if (background == Color.TRANSPARENT) {
                "TRANSPARENT · WALLPAPER SHOWS THROUGH"
            } else {
                "SOLID BACKGROUND · " +
                    (backgrounds.firstOrNull {
                        it.color == background
                    }?.label ?: "CUSTOM")
            }
    }
}
