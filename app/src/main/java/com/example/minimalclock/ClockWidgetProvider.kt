package com.example.minimalclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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

            val foregroundColor = prefs.getInt("foreground", Color.WHITE)
            val backgroundColor = prefs.getInt("background", Color.TRANSPARENT)
            val daySize = prefs.getInt("day_size", 34)
            val timeSize = prefs.getInt("time_size", 58)
            val dateSize = prefs.getInt("date_size", 22)
            val alignment = prefs.getInt("text_alignment", 1)
            val timeFormat = prefs.getInt("time_format", 24)
            val position = prefs.getString("position", "middle-center") ?: "middle-center"

            val views = RemoteViews(context.packageName, R.layout.clock_widget_updated)

            // 1. Time and Date
            val now = Date()
            val locale = Locale.getDefault()

            val weekdayText = SimpleDateFormat("EEEE", locale).format(now).uppercase(locale)
            val dateText = SimpleDateFormat("MMMM dd", locale).format(now).uppercase(locale)
            val timePattern = if (timeFormat == 12) "hh:mm a" else "HH:mm"
            val timeText = SimpleDateFormat(timePattern, locale).format(now).uppercase(locale)

            views.setTextViewText(R.id.weekday, weekdayText)
            views.setTextViewText(R.id.time, timeText)
            views.setTextViewText(R.id.date, dateText)

            // 2. Colors
            views.setTextColor(R.id.weekday, foregroundColor)
            views.setTextColor(R.id.time, foregroundColor)
            views.setTextColor(R.id.date, foregroundColor)

            // 3. Sizes
            views.setTextViewTextSize(R.id.weekday, android.util.TypedValue.COMPLEX_UNIT_SP, daySize.toFloat())
            views.setTextViewTextSize(R.id.time, android.util.TypedValue.COMPLEX_UNIT_SP, timeSize.toFloat())
            views.setTextViewTextSize(R.id.date, android.util.TypedValue.COMPLEX_UNIT_SP, dateSize.toFloat())

            // 4. Background
            if (backgroundColor == Color.TRANSPARENT) {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
            } else {
                views.setInt(R.id.widget_root, "setBackgroundColor", backgroundColor)
            }

            // 5. Horizontal Alignment (Left/Centre/Right)
            val horizGravity = when (alignment) {
                0 -> Gravity.LEFT
                2 -> Gravity.RIGHT
                else -> Gravity.CENTER_HORIZONTAL
            }
            views.setInt(R.id.widget_text_container, "setGravity", horizGravity)

            // 6. Vertical Position (Top/Middle/Bottom)
            val vertGravity = when {
                position.startsWith("top") -> Gravity.TOP
                position.startsWith("bottom") -> Gravity.BOTTOM
                else -> Gravity.CENTER_VERTICAL
            }
            views.setInt(R.id.widget_root, "setGravity", vertGravity)

            // 7. Click to open settings
            val configIntent = Intent(context, SettingsActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(
                context, 0, configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
