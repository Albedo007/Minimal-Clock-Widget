package com.example.minimalclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
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
        val alarm = context.getSystemService(Context.ALARM_SERVICE)
            as AlarmManager
        alarm.cancel(tickPendingIntent(context))
    }

    companion object {
        private const val ACTION_TICK =
            "com.example.minimalclock.ACTION_TICK"

        private val timeViewIds = intArrayOf(
            R.id.time_thin,
            R.id.time_light,
            R.id.time_regular,
            R.id.time_medium,
            R.id.time_bold
        )

        fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            id: Int
        ) {
            val prefs = context.getSharedPreferences(
                "clock_settings",
                Context.MODE_PRIVATE
            )

            val views = RemoteViews(
                context.packageName,
                R.layout.clock_widget
            )

            val now = Date()
            val locale = Locale.getDefault()

            val weekday = SimpleDateFormat("EEEE", locale)
                .format(now).uppercase(locale)

            val time = SimpleDateFormat("HH:mm", locale)
                .format(now)

            val date = SimpleDateFormat("MMMM dd", locale)
                .format(now).uppercase(locale)

            views.setTextViewText(R.id.weekday, weekday)
            views.setTextViewText(R.id.date, date)

            val foreground = prefs.getInt("foreground", Color.WHITE)
            val background = prefs.getInt("background", Color.TRANSPARENT)

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                background
            )

            views.setTextColor(R.id.weekday, foreground)
            views.setTextColor(R.id.date, foreground)

            val selectedWeight = prefs.getInt("weight", 0)
                .coerceIn(0, 4)

            timeViewIds.forEachIndexed { index, viewId ->
                views.setTextViewText(viewId, time)
                views.setTextColor(viewId, foreground)

                views.setTextViewTextSize(
                    viewId,
                    2,
                    prefs.getInt("time_size", 58).coerceAtLeast(10)
                        .toFloat()
                )

                views.setViewVisibility(
                    viewId,
                    if (index == selectedWeight) View.VISIBLE
                    else View.GONE
                )
            }

            views.setTextViewTextSize(
                R.id.weekday,
                2,
                prefs.getInt("day_size", 34).coerceAtLeast(10).toFloat()
            )

            views.setTextViewTextSize(
                R.id.date,
                2,
                prefs.getInt("date_size", 22).coerceAtLeast(10).toFloat()
            )

            manager.updateAppWidget(id, views)
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)

            val ids = manager.getAppWidgetIds(
                ComponentName(
                    context,
                    ClockWidgetProvider::class.java
                )
            )

            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockWidgetProvider::class.java)
                .apply {
                    action = ACTION_TICK
                }

            return PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun scheduleUpdates(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

            val pendingIntent = tickPendingIntent(context)

            val nextMinute = System.currentTimeMillis() -
                (System.currentTimeMillis() % 60000L) + 60000L

            alarm.setRepeating(
                AlarmManager.RTC,
                nextMinute,
                60000L,
                pendingIntent
            )
        }
    }
}
