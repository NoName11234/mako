package com.rama.colibri

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.view.View

/* ---------- Pure data & formatting ---------- */

data class StatusInfo(
    val date: String?,
    val temperature: String?,
    val battery: String?
)

fun buildStatusLine(info: StatusInfo): String {
    return listOfNotNull(
        info.date,
        info.temperature,
        info.battery
    ).joinToString("  |  ")
}

/* ---------- Activity ---------- */

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var listView: ListView

    private var currentInfo = StatusInfo(
        date = null,
        temperature = null,
        battery = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide status bar for API 26+
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.date)
        listView = findViewById(R.id.appList)

        setupAppList()

        // Initial demo values (replace later)
        updateDate("20 Dez 2025")
        updateTemperature("33°F")
        updateBattery("42%")
    }

    /* ---------- UI updates ---------- */

    private fun updateDate(date: String) {
        currentInfo = currentInfo.copy(date = date)
        refreshStatusLine()
    }

    private fun updateTemperature(temp: String) {
        currentInfo = currentInfo.copy(temperature = temp)
        refreshStatusLine()
    }

    private fun updateBattery(battery: String) {
        currentInfo = currentInfo.copy(battery = battery)
        refreshStatusLine()
    }

    private fun refreshStatusLine() {
        statusText.text = buildStatusLine(currentInfo)
    }

    /* ---------- App list ---------- */

    private fun setupAppList() {
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps: List<ResolveInfo> =
            pm.queryIntentActivities(intent, 0)
                .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val labels = apps.map { it.loadLabel(pm).toString() }

        listView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            labels
        )

        listView.setOnItemClickListener { _, _, position, _ ->
            val app = apps[position]

            val launchIntent = Intent().apply {
                setClassName(
                    app.activityInfo.packageName,
                    app.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(launchIntent)
        }
    }
}
