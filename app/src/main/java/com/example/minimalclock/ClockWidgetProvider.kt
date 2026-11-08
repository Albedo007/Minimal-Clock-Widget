package com.example.minimalclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        // Helper function to trigger an update from SettingsActivity
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ClockWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)

            // Load all settings from SharedPreferences
            val foregroundColor = prefs.getInt("foreground", Color.WHITE)
            val backgroundColor = prefs.getInt("background", Color.TRANSPARENT)
            val daySize = prefs.getInt("day_size", 34)
            val timeSize = prefs.getInt("time_size", 58)
            val dateSize = prefs.getInt("date_size", 22)
            val padding = prefs.getInt("padding", 12)
            val alignment = prefs.getInt("text_alignment", 1)
            val weight = prefs.getInt("weight", 0)
            val timeFormat = prefs.getInt("time_format", 24)
            val weekdayFont = prefs.getInt("weekday_font", 0)
            val timeFont = prefs.getInt("time_font", 0)
            val dateFont = prefs.getInt("date_font", 0)
            val position = prefs.getString("position", "middle-center") ?: "middle-center"

            // Initialize RemoteViews with your updated layout
            val views = RemoteViews(context.packageName, R.layout.clock_widget_updated)

            // 1. Setup the Time and Date
            val now = Date()
            val locale = Locale.getDefault()

            val weekdayText = SimpleDateFormat("EEEE", locale).format(now).uppercase(locale)
            val dateText = SimpleDateFormat("MMMM dd", locale).format(now).uppercase(locale)
            val timePattern = if (timeFormat == 12) "hh:mm a" else "HH:mm"
            val timeText = SimpleDateFormat(timePattern, locale).format(now).uppercase(locale)

            views.setTextViewText(R.id.weekday, weekdayText)
            views.setTextViewText(R.id.time, timeText)
            views.setTextViewText(R.id.date, dateText)

            // 2. Setup Text Colors
            views.setTextColor(R.id.weekday, foregroundColor)
            views.setTextColor(R.id.time, foregroundColor)
            views.setTextColor(R.id.date, foregroundColor)

            // 3. Setup Text Sizes
            views.setTextViewTextSize(R.id.weekday, android.util.TypedValue.COMPLEX_UNIT_SP, daySize.toFloat())
            views.setTextViewTextSize(R.id.time, android.util.TypedValue.COMPLEX_UNIT_SP, timeSize.toFloat())
            views.setTextViewTextSize(R.id.date, android.util.TypedValue.COMPLEX_UNIT_SP, dateSize.toFloat())

            // 4. Setup Font Families
            val fontFamilies = listOf("sans-serif", "sans-serif-light", "serif", "monospace", "sans-serif-condensed")
            
            val selectedDayFont = fontFamilies.getOrElse(weekdayFont) { "sans-serif" }
            val selectedTimeFont = fontFamilies.getOrElse(timeFont) { "sans-serif" }
            val selectedDateFont = fontFamilies.getOrElse(dateFont) { "sans-serif" }

            // Note: RemoteViews cannot easily change fonts dynamically without a helper library.
            // We use setInt with "setTypeface" here, but be aware that some launchers may ignore it.
            views.setInt(R.id.weekday, "setTypeface", Typeface.create(selectedDayFont, Typeface.NORMAL).style)
            views.setInt(R.id.date, "setTypeface", Typeface.create(selectedDateFont, Typeface.NORMAL).style)
            
            // Time Font Weight Override
            val timeTypeface = if (timeFont == 0) {
                val weightName = listOf("sans-serif-thin", "sans-serif-light", "sans-serif", "sans-serif-medium", "sans-serif")[weight.coerceIn(0, 4)]
                if (weight == 4) Typeface.create(weightName, Typeface.BOLD) else Typeface.create(weightName, Typeface.NORMAL)
            } else {
                Typeface.create(selectedTimeFont, Typeface.NORMAL)
            }
            views.setInt(R.id.time, "setTypeface", timeTypeface.style)

            // 5. Setup Background
            if (backgroundColor == Color.TRANSPARENT) {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
            } else {
                views.setInt(R.id.widget_root, "setBackgroundColor", backgroundColor)
            }

            // 6. Setup Padding
            val density = context.resources.displayMetrics.density
            val paddingPx = (padding * density + 0.5f).toInt()
            views.setViewPadding(R.id.widget_root, paddingPx, paddingPx, paddingPx, paddingPx)

            // 7. Setup Text Alignment (Horizontal)
            val horizGravity = when (alignment) {
                0 -> Gravity.LEFT
                2 -> Gravity.RIGHT
                else -> Gravity.CENTER_HORIZONTAL
            }
            // Apply horizontal alignment to the inner container we added in the XML
            views.setInt(R.id.widget_text_container, "setGravity", horizGravity)

            // 8. Setup Widget Position (Vertical)
            val vertGravity = when {
                position.startsWith("top") -> Gravity.TOP
                position.startsWith("bottom") -> Gravity.BOTTOM
                else -> Gravity.CENTER_VERTICAL
            }
            // Apply vertical gravity to the root layout so the whole clock moves up/down
            views.setInt(R.id.widget_root, "setGravity", vertGravity)

            // 9. Optional: Clicking the widget opens the SettingsActivity
            val configIntent = Intent(context, SettingsActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(
                context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent)

            // Finally, update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
