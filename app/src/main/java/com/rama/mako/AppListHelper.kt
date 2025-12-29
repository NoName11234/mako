package com.rama.mako

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class AppListHelper(
    private val context: Context,
    private val listView: ListView
) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private var openActionsFor: String? = null // currently open row actions

    fun setup() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0).toMutableList()

        fun sortApps() {
            apps.sortWith(
                compareByDescending<ResolveInfo> {
                    prefs.getBoolean(it.activityInfo.packageName, false)
                }.thenBy {
                    it.loadLabel(pm).toString().lowercase()
                }
            )
        }

        sortApps()

        val adapter = object : ArrayAdapter<ResolveInfo>(
            context,
            R.layout.app_list_item,
            R.id.open_app_button,
            apps
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val app = getItem(position) ?: return view
                val pkg = app.activityInfo.packageName

                val label = view.findViewById<TextView>(R.id.open_app_button)
                val emptySpace = view.findViewById<View>(R.id.empty_space)
                val favButton = view.findViewById<View>(R.id.favorite_button)
                val favIcon = view.findViewById<ImageView>(R.id.favorite_icon)
                val closeButton = view.findViewById<View>(R.id.close_button)
                val actions = view.findViewById<View>(R.id.actions_container)
                val bottomBorder = view.findViewById<View>(R.id.favorite_bottom_border)

                label.text = app.loadLabel(pm)

                // Restore favorite state
                favIcon.isSelected = prefs.getBoolean(pkg, false)

                // Show/hide actions
                actions.visibility = if (openActionsFor == pkg) View.VISIBLE else View.GONE

                // Tap on label → launch app
                label.setOnClickListener {
                    val launchIntent = Intent().apply {
                        setClassName(pkg, app.activityInfo.name)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    try {
                        context.startActivity(launchIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                        prefs.edit().remove(pkg).apply()
                        apps.remove(app)
                        openActionsFor = null
                        notifyDataSetChanged()
                    }
                }

                // Long press on label → open row actions
                label.setOnLongClickListener {
                    openActionsFor = pkg
                    notifyDataSetChanged()
                    true
                }

                // Long press on empty space → open global settings
                emptySpace.setOnLongClickListener {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                    true
                }

                // Favorite toggle
                favButton.setOnClickListener {
                    val newState = !favIcon.isSelected
                    favIcon.isSelected = newState
                    prefs.edit().putBoolean(pkg, newState).apply()
                    sortApps()
                    notifyDataSetChanged()
                    openActionsFor = null
                    notifyDataSetChanged()
                }

                // Close actions
                closeButton.setOnClickListener {
                    openActionsFor = null
                    notifyDataSetChanged()
                }

                // Show bottom border only for last favorite
                val isFavorite = favIcon.isSelected
                val isLastFavorite = isFavorite && (
                        position == apps.size - 1 ||
                                !prefs.getBoolean(
                                    getItem(position + 1)!!.activityInfo.packageName,
                                    false
                                )
                        )
                bottomBorder.visibility = if (isLastFavorite) View.VISIBLE else View.GONE

                return view
            }
        }

        listView.adapter = adapter

        // Auto-hide actions when scrolling
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE && openActionsFor != null) {
                    openActionsFor = null
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) = Unit
        })
    }
}
