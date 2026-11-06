package com.example.minimalclock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.RemoteViews

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE)
            val views = RemoteViews(context.packageName, R.layout.clock_widget)

            val foreground = prefs.getInt("foreground", Color.WHITE)
            val background = prefs.getInt("background", Color.TRANSPARENT)

            views.setInt(R.id.widget_root, "setBackgroundColor", background)

            views.setTextColor(R.id.weekday, foreground)
            views.setTextColor(R.id.time, foreground)
            views.setTextColor(R.id.date, foreground)

            val weight = when (prefs.getInt("weight", 0)) {
                0 -> Typeface.create("sans-serif-thin", Typeface.NORMAL)
                1 -> Typeface.create("sans-serif-light", Typeface.NORMAL)
                2 -> Typeface.create("sans-serif", Typeface.NORMAL)
                3 -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
                else -> Typeface.create("sans-serif", Typeface.BOLD)
            }

            views.setInt(R.id.weekday, "setTypeface", weight.style)
            views.setInt(R.id.time, "setTypeface", weight.style)
            views.setInt(R.id.date, "setTypeface", weight.style)

            views.setTextViewTextSize(R.id.weekday, 0, prefs.getInt("day_size", 34).toFloat())
            views.setTextViewTextSize(R.id.time, 0, prefs.getInt("time_size", 58).toFloat())
            views.setTextViewTextSize(R.id.date, 0, prefs.getInt("date_size", 22).toFloat())

            manager.updateAppWidget(id, views)
        }

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ClockWidgetProvider::class.java)
            )
            ids.forEach { updateWidget(context, manager, it) }
        }
    }
}
