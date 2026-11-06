package com.example.minimalclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
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
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(tickPendingIntent(context))
    }

    companion object {
        private const val ACTION_TICK =
            "com.example.minimalclock.ACTION_TICK"

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

            views.setTextViewText(
                R.id.weekday,
                SimpleDateFormat("EEEE", locale)
                    .format(now).uppercase(locale)
            )

            views.setTextViewText(
                R.id.time,
                SimpleDateFormat("HH:mm", locale).format(now)
            )

            views.setTextViewText(
                R.id.date,
                SimpleDateFormat("MMMM dd", locale)
                    .format(now).uppercase(locale)
            )

            val foreground = prefs.getInt("foreground", Color.WHITE)
            val background = prefs.getInt("background", Color.TRANSPARENT)

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                background
            )

            views.setTextColor(R.id.weekday, foreground)
            views.setTextColor(R.id.time, foreground)
            views.setTextColor(R.id.date, foreground)

            views.setTextViewTextSize(
                R.id.weekday, 2,
                prefs.getInt("day_size", 34).toFloat()
            )
            views.setTextViewTextSize(
                R.id.time, 2,
                prefs.getInt("time_size", 58).toFloat()
            )
            views.setTextViewTextSize(
                R.id.date, 2,
                prefs.getInt("date_size", 22).toFloat()
            )

            manager.updateAppWidget(id, views)
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ClockWidgetProvider::class.java)
            )
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
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
