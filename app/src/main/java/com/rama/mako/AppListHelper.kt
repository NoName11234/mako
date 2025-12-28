package com.rama.mako

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast

class AppListHelper(
    private val context: Context,
    private val listView: ListView
) {

    private val prefs =
        context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun setup() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        val labels = apps.map { it.loadLabel(pm).toString() }

        val adapter = object : ArrayAdapter<String>(
            context,
            R.layout.app_list_item,
            R.id.text1,
            labels
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)

                val app = apps[position]
                val pkg = app.activityInfo.packageName

                val favButton = view.findViewById<View>(R.id.favorite_button)
                val favIcon = view.findViewById<ImageView>(R.id.favorite_icon)

                // restore state
                favIcon.isSelected = prefs.getBoolean(pkg, false)

                favButton.setOnClickListener {
                    val newState = !favIcon.isSelected
                    favIcon.isSelected = newState

                    prefs.edit()
                        .putBoolean(pkg, newState)
                        .apply()
                }

                return view
            }
        }

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position >= apps.size) return@setOnItemClickListener

            val app = apps[position]
            val launchIntent = Intent().apply {
                setClassName(
                    app.activityInfo.packageName,
                    app.activityInfo.name
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "App not found or uninstalled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
