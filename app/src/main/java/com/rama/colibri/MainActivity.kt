package com.example.minimallauncher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.appList)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val labels = apps.map { it.loadLabel(pm).toString() }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            labels
        )

        listView.adapter = adapter

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
