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
    private lateinit var positionGrid: GridLayout
    private lateinit var alignmentRow: LinearLayout
    private val draft = mutableMapOf<String, Int>()
    private var draftPosition = "middle-center"
    private val positionNames = listOf(
        "top-left", "top-center", "top-right",
        "middle-left", "middle-center", "middle-right",
        "bottom-left", "bottom-center", "bottom-right"
    )

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
            text = value.uppercase(Locale.getDefault())
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

        listOf("foreground", "background", "day_size", "time_size", "date_size", "text_alignment", "weight", "time_format", "weekday_font", "time_font", "date_font").forEach { key ->
            val default = when (key) {
                "foreground" -> Color.WHITE
                "background" -> Color.TRANSPARENT
                "day_size" -> 34
                "time_size" -> 58
                "date_size" -> 22
                "text_alignment" -> 1
                "time_format" -> 24
                else -> 0
            }
            draft[key] = savedInstanceState?.getInt("draft_$key")
                ?: prefs.getInt(key, default)
        }
        draftPosition = savedInstanceState?.getString("draft_position")
            ?: prefs.getString("position", "middle-center") ?: "middle-center"
        if (draftPosition !in positionNames) draftPosition = "middle-center"

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
            setPadding(dp(12), dp(12), dp(12), dp(12))
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

        previewSurface.addView(
            previewWeekday,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        previewSurface.addView(
            previewTime,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
                bottomMargin = dp(8)
            }
        )

        previewSurface.addView(
            previewDate,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

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

        // POSITION SELECTOR
        addSection(content, "POSITION", "Choose where the clock sits in the widget")
        val positionCard = newCard()
        positionGrid = GridLayout(this).apply {
            columnCount = 3
            rowCount = 3
            useDefaultMargins = false
        }
        val positionSymbols = listOf("↖", "↑", "↗", "←", "●", "→", "↙", "↓", "↘")
        positionNames.forEachIndexed { index, position ->
            val cell = text(positionSymbols[index], 23f, white).apply {
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    draftPosition = position
                    updatePositionSelection()
                    refreshPreview()
                }
            }
            positionGrid.addView(cell, GridLayout.LayoutParams().apply {
                width = 0
                height = dp(54)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            })
        }
        positionCard.addView(positionGrid)
        content.addView(positionCard)
        updatePositionSelection()

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

        // TEXT ALIGNMENT
        addSection(content, "TEXT ALIGNMENT", "Align the clock text left, centre or right")
        val alignmentCard = newCard()
        alignmentRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf("LEFT", "CENTRE", "RIGHT").forEachIndexed { index, label ->
            val chip = text(label, 12f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(13), dp(8), dp(13))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    draft["text_alignment"] = index
                    updateAlignmentSelection()
                    refreshPreview()
                }
                layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    marginEnd = dp(6)
                }
            }
            alignmentRow.addView(chip)
        }
        alignmentCard.addView(alignmentRow)
        content.addView(alignmentCard)
        updateAlignmentSelection()

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
                    draft["weight"] = index
                    refreshPreview()

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
            draft["weight"]?.coerceIn(0, 4) ?: 0
        )

        // TIME FORMAT
        addSection(content, "TIME FORMAT", "Choose 12-hour or 24-hour time")
        val formatCard = newCard()
        val formatRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf(12, 24).forEach { format ->
            val chip = text(if (format == 12) "12-HOUR · AM/PM" else "24-HOUR", 12f, white, true).apply {
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(13), dp(12), dp(13))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    draft["time_format"] = format
                    updateFormatSelection(formatRow)
                    refreshPreview()
                }
                layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginEnd = dp(6) }
            }
            formatRow.addView(chip)
        }
        formatCard.addView(formatRow)
        content.addView(formatCard)
        updateFormatSelection(formatRow)

        // INDIVIDUAL FONT SELECTION
        addSection(content, "FONT FAMILY", "Choose a separate font for each section")
        val fontCard = newCard()
        addFontSelector(fontCard, "Weekday font", "weekday_font")
        addDivider(fontCard)
        addFontSelector(fontCard, "Time font", "time_font")
        addDivider(fontCard)
        addFontSelector(fontCard, "Date font", "date_font")
        content.addView(fontCard)

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

        // SAVE AND RESET BUTTONS
        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(26), 0, dp(8))
        }

        val resetButton = text("RESET", 13f, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(panelSoft, 14, Color.rgb(65, 65, 72))
            setPadding(dp(18), dp(15), dp(18), dp(15))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                draft["foreground"] = Color.WHITE
                draft["background"] = Color.TRANSPARENT
                draft["day_size"] = 34
                draft["time_size"] = 58
                draft["date_size"] = 22
                draft["text_alignment"] = 1
                draft["weight"] = 0
                draft["time_format"] = 24
                draft["weekday_font"] = 0
                draft["time_font"] = 0
                draft["date_font"] = 0
                draftPosition = "middle-center"
                refreshPreview()
                updatePositionSelection()
                updateAlignmentSelection()
                Toast.makeText(this@SettingsActivity, "Defaults restored in preview. Tap Save to apply.", Toast.LENGTH_SHORT).show()
            }
        }
        val saveButton = text("SAVE", 13f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            background = rounded(accent, 14)
            setPadding(dp(30), dp(15), dp(30), dp(15))
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val editor = prefs.edit()
                draft.forEach { (key, value) -> editor.putInt(key, value) }
                editor.putString("position", draftPosition)
                editor.apply()
                ClockWidgetProvider.updateAll(this@SettingsActivity)
                Toast.makeText(this@SettingsActivity, "Clock settings saved", Toast.LENGTH_SHORT).show()
            }
        }
        buttonRow.addView(resetButton, LinearLayout.LayoutParams(0, dp(50), 1f).apply { marginEnd = dp(8) })
        buttonRow.addView(saveButton, LinearLayout.LayoutParams(0, dp(50), 1.5f))
        content.addView(buttonRow)

        val footer = text(
            "Changes are applied to your home-screen widget only after you tap Save.",
            12f, muted
        ).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
        content.addView(footer)

        setContentView(scroll)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        draft.forEach { (key, value) -> outState.putInt("draft_$key", value) }
        outState.putString("draft_position", draftPosition)
    }

    private val fontFamilies = listOf("sans-serif", "sans-serif-light", "serif", "monospace", "sans-serif-condensed")
    private val fontLabels = listOf("Modern", "Light", "Serif", "Mono", "Condensed")
    private fun addFontSelector(parent: LinearLayout, label: String, key: String) {
        val block = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(12), 0, dp(12)) }
        block.addView(text(label, 14f, white))
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(8), 0, 0) }
        fontLabels.forEachIndexed { index, name ->
            val chip = text(name, 10f, white).apply {
                gravity = Gravity.CENTER
                setPadding(dp(7), dp(11), dp(7), dp(11))
                isClickable = true
                isFocusable = true
                setOnClickListener { draft[key] = index; updateFontSelection(row, key); refreshPreview() }
                layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f).apply { marginEnd = dp(4) }
            }
            row.addView(chip)
        }
        block.addView(row)
        parent.addView(block)
        updateFontSelection(row, key)
    }

    private fun updateFontSelection(row: LinearLayout, key: String) {
        val selected = (draft[key] ?: 0).coerceIn(0, fontLabels.lastIndex)
        for (i in 0 until row.childCount) {
            val chip = row.getChildAt(i) as TextView
            chip.background = rounded(if (i == selected) Color.rgb(75, 20, 31) else panelSoft, 10, if (i == selected) accent else Color.rgb(65, 65, 72))
            chip.setTextColor(if (i == selected) accent else white)
        }
    }

    private fun updateFormatSelection(row: LinearLayout) {
        val selected = if ((draft["time_format"] ?: 24) == 12) 0 else 1
        for (i in 0 until row.childCount) {
            val chip = row.getChildAt(i) as TextView
            chip.background = rounded(if (i == selected) Color.rgb(75, 20, 31) else panelSoft, 12, if (i == selected) accent else Color.rgb(65, 65, 72))
            chip.setTextColor(if (i == selected) accent else white)
        }
    }

    private fun updatePositionSelection() {
        if (!::positionGrid.isInitialized) return
        for (i in 0 until positionGrid.childCount) {
            val cell = positionGrid.getChildAt(i) as TextView
            val selected = positionNames[i] == draftPosition
            cell.background = rounded(
                if (selected) Color.rgb(75, 20, 31) else panelSoft,
                12,
                if (selected) accent else Color.rgb(65, 65, 72)
            )
            cell.setTextColor(if (selected) accent else white)
        }
    }

    private fun positionGravity(): Int {
        return when {
            draftPosition.startsWith("top") -> Gravity.TOP
            draftPosition.startsWith("bottom") -> Gravity.BOTTOM
            else -> Gravity.CENTER_VERTICAL
        }
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

            progress = (draft[key] ?: prefs.getInt(key, default))
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
                        draft[key] = actual
                        refreshPreview()
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
        val selected = draft[preferenceKey] ?: prefs.getInt(
            preferenceKey,
            if (preferenceKey == "foreground") Color.WHITE else Color.TRANSPARENT
        )

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        choices.forEach { choice ->

            val selectedNow = choice.color == selected

            val chip = TextView(this).apply {

                text = if (selectedNow)
                    "✓ ${choice.label.uppercase(Locale.getDefault())}"
                else
                    choice.label.uppercase(Locale.getDefault())

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

                    draft[preferenceKey] = choice.color
                    refreshPreview()
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

    private fun updateAlignmentSelection() {
        if (!::alignmentRow.isInitialized) return
        val selected = (draft["text_alignment"] ?: 1).coerceIn(0, 2)
        for (i in 0 until alignmentRow.childCount) {
            val chip = alignmentRow.getChildAt(i) as TextView
            chip.background = rounded(
                if (i == selected) Color.rgb(75, 20, 31) else panelSoft,
                12,
                if (i == selected) accent else Color.rgb(65, 65, 72)
            )
            chip.setTextColor(if (i == selected) accent else white)
        }
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

        val timePattern = if ((draft["time_format"] ?: 24) == 12) "hh:mm a" else "HH:mm"
        previewTime.text = SimpleDateFormat(timePattern, locale).format(now).uppercase(locale)

        previewDate.text =
            SimpleDateFormat("MMMM dd", locale)
                .format(now)
                .uppercase(locale)

        val foreground = draft["foreground"] ?: Color.WHITE

        previewWeekday.setTextColor(foreground)
        previewTime.setTextColor(foreground)
        previewDate.setTextColor(foreground)

        previewWeekday.textSize =
            (draft["day_size"] ?: 34).coerceAtLeast(10).toFloat()

        previewTime.textSize =
            (draft["time_size"] ?: 58).coerceAtLeast(10).toFloat()

        previewDate.textSize =
            (draft["date_size"] ?: 22).coerceAtLeast(10).toFloat()

        val weight = (draft["weight"] ?: 0).coerceIn(0, 4)

        val selectedFont = (draft["time_font"] ?: 0).coerceIn(0, fontFamilies.lastIndex)
        val timeTypefaceName = if (selectedFont == 0) {
            listOf("sans-serif-thin", "sans-serif-light", "sans-serif", "sans-serif-medium", "sans-serif")[weight]
        } else fontFamilies[selectedFont]
        previewTime.typeface = Typeface.create(timeTypefaceName, if (weight == 4) Typeface.BOLD else Typeface.NORMAL)
        previewWeekday.typeface = Typeface.create(fontFamilies[(draft["weekday_font"] ?: 0).coerceIn(0, fontFamilies.lastIndex)], Typeface.NORMAL)
        previewDate.typeface = Typeface.create(fontFamilies[(draft["date_font"] ?: 0).coerceIn(0, fontFamilies.lastIndex)], Typeface.NORMAL)

        val background = draft["background"] ?: Color.TRANSPARENT
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
        
        val alignmentGravity = when ((draft["text_alignment"] ?: 1).coerceIn(0, 2)) {
            0 -> Gravity.LEFT
            2 -> Gravity.RIGHT
            else -> Gravity.CENTER_HORIZONTAL
        }
        
        previewSurface.gravity = alignmentGravity or positionGravity()

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
