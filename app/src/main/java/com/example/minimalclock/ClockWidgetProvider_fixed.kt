package com.example.minimalclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
        scheduleUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            updateAll(context)
            scheduleUpdates(context)
        }
    }

    override fun onEnabled(context: Context) {
        scheduleUpdates(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(tickPendingIntent(context))
    }

    companion object {
        private const val ACTION_TICK = "com.example.minimalclock.ACTION_TICK"
        private const val PREFS_NAME = "clock_settings"

        private val weekdayIds = intArrayOf(
            R.id.weekday_font_0, R.id.weekday_font_1, R.id.weekday_font_2,
            R.id.weekday_font_3, R.id.weekday_font_4
        )
        private val dateIds = intArrayOf(
            R.id.date_font_0, R.id.date_font_1, R.id.date_font_2,
            R.id.date_font_3, R.id.date_font_4
        )
        private val timeIds = Array(5) { family ->
            val allIds = intArrayOf(
                R.id.time_f0_w0, R.id.time_f0_w1, R.id.time_f0_w2, R.id.time_f0_w3, R.id.time_f0_w4,
                R.id.time_f1_w0, R.id.time_f1_w1, R.id.time_f1_w2, R.id.time_f1_w3, R.id.time_f1_w4,
                R.id.time_f2_w0, R.id.time_f2_w1, R.id.time_f2_w2, R.id.time_f2_w3, R.id.time_f2_w4,
                R.id.time_f3_w0, R.id.time_f3_w1, R.id.time_f3_w2, R.id.time_f3_w3, R.id.time_f3_w4,
                R.id.time_f4_w0, R.id.time_f4_w1, R.id.time_f4_w2, R.id.time_f4_w3, R.id.time_f4_w4
            )
            allIds.copyOfRange(family * 5, family * 5 + 5)
        }

        private val allTextIds: IntArray
            get() = weekdayIds + dateIds + timeIds.flatMap { it.asIterable() }.toIntArray()

        fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.clock_widget)
            val now = Date()
            val locale = Locale.getDefault()

            val weekday = SimpleDateFormat("EEEE", locale).format(now).uppercase(locale)
            val timePattern = if (prefs.getInt("time_format", 24) == 12) "hh:mm a" else "HH:mm"
            val time = SimpleDateFormat(timePattern, Locale.US)
    .format(now)
    .uppercase(Locale.US)
            val date = SimpleDateFormat("MMMM dd", locale).format(now).uppercase(locale)

            val foreground = prefs.getInt("foreground", Color.WHITE)
            val background = prefs.getInt("background", Color.TRANSPARENT)

            weekdayIds.forEach { views.setTextViewText(it, weekday) }
            dateIds.forEach { views.setTextViewText(it, date) }

            views.setInt(R.id.widget_root, "setBackgroundColor", background)

            // LEFT=0, CENTRE=1, RIGHT=2 in SettingsActivity.
            // Convert those app values to Android Gravity constants.
            val textGravity = when (prefs.getInt("text_alignment", 1).coerceIn(0, 2)) {
                0 -> Gravity.START or Gravity.CENTER_VERTICAL
                2 -> Gravity.END or Gravity.CENTER_VERTICAL
                else -> Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
            }
            allTextIds.forEach { viewId ->
                views.setInt(viewId, "setGravity", textGravity)
                views.setTextColor(viewId, foreground)
            }

            // Apply the padding preference to the whole widget, in pixels.
            val paddingDp = prefs.getInt("padding", 12).coerceIn(0, 40)
            val paddingPx = (paddingDp * context.resources.displayMetrics.density + 0.5f).toInt()
            views.setViewPadding(R.id.widget_root, paddingPx, paddingPx, paddingPx, paddingPx)

            // Keep the existing 3x3 vertical placement control.
            // Horizontal text alignment is controlled separately above.
            val position = prefs.getString("position", "middle-center") ?: "middle-center"
            val verticalGravity = when {
                position.startsWith("top") -> Gravity.TOP
                position.startsWith("bottom") -> Gravity.BOTTOM
                else -> Gravity.CENTER_VERTICAL
            }
            views.setInt(R.id.widget_root, "setGravity", Gravity.CENTER_HORIZONTAL or verticalGravity)

            val dayFont = prefs.getInt("weekday_font", 0).coerceIn(0, 4)
            weekdayIds.forEachIndexed { index, viewId ->
                views.setTextViewTextSize(viewId, 2, prefs.getInt("day_size", 34).coerceIn(10, 100).toFloat())
                views.setViewVisibility(viewId, if (index == dayFont) View.VISIBLE else View.GONE)
            }

            val dateFont = prefs.getInt("date_font", 0).coerceIn(0, 4)
            dateIds.forEachIndexed { index, viewId ->
                views.setTextViewTextSize(viewId, 2, prefs.getInt("date_size", 22).coerceIn(10, 100).toFloat())
                views.setViewVisibility(viewId, if (index == dateFont) View.VISIBLE else View.GONE)
            }

            val timeFont = prefs.getInt("time_font", 0).coerceIn(0, 4)
            val selectedWeight = prefs.getInt("weight", 0).coerceIn(0, 4)
            timeIds.forEachIndexed { familyIndex, familyIds ->
                familyIds.forEachIndexed { weightIndex, viewId ->
                    views.setTextViewText(viewId, time)
                    views.setTextViewTextSize(viewId, 2, prefs.getInt("time_size", 58).coerceIn(10, 120).toFloat())
                    views.setViewVisibility(
                        viewId,
                        if (familyIndex == timeFont && weightIndex == selectedWeight) View.VISIBLE else View.GONE
                    )
                }
            }

            manager.updateAppWidget(id, views)
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockWidgetProvider::class.java).apply { action = ACTION_TICK }
            return PendingIntent.getBroadcast(
                context, 1001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun scheduleUpdates(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = tickPendingIntent(context)
            val now = System.currentTimeMillis()
            val nextMinute = now - (now % 60000L) + 60000L
            alarm.setRepeating(AlarmManager.RTC, nextMinute, 60000L, pendingIntent)
        }
    }
}
