package com.rama.mako

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
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

    // ------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------

    private val prefs =
        context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val pm = context.packageManager
    private val apps = mutableListOf<ResolveInfo>()

    private lateinit var adapter: ArrayAdapter<ResolveInfo>

    private var openActionsFor: String? = null

    // ------------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------------

    fun setup() {
        loadApps()
        sortApps()
        setupAdapter()
        setupScrollListener()
    }

    fun refresh() {
        openActionsFor = null
        loadApps()
        sortApps()
        adapter.notifyDataSetChanged()
    }

    // ------------------------------------------------------------------------
    // Data
    // ------------------------------------------------------------------------

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        apps.clear()
        apps.addAll(pm.queryIntentActivities(intent, 0))
    }

    private fun sortApps() {
        apps.sortWith(
            compareByDescending<ResolveInfo> {
                prefs.getBoolean(it.activityInfo.packageName, false)
            }.thenBy {
                it.loadLabel(pm).toString().lowercase()
            }
        )
    }

    private fun isFavorite(pkg: String): Boolean =
        prefs.getBoolean(pkg, false)

    private fun setFavorite(pkg: String, value: Boolean) {
        prefs.edit().putBoolean(pkg, value).apply()
    }

    private fun removeFavorite(pkg: String) {
        prefs.edit().remove(pkg).apply()
    }

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------

    private fun launchApp(pkg: String) {
        val intent = pm.getLaunchIntentForPackage(pkg)

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                "Unable to launch app",
                Toast.LENGTH_SHORT
            ).show()

            removeFavorite(pkg)
            openActionsFor = null
            refresh()
        }
    }

    private fun openRowActions(pkg: String) {
        openActionsFor = pkg
        adapter.notifyDataSetChanged()
    }

    private fun closeRowActions() {
        openActionsFor = null
        adapter.notifyDataSetChanged()
    }

    private fun openAppSettings(pkg: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", pkg, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ------------------------------------------------------------------------
    // Adapter
    // ------------------------------------------------------------------------

    private fun setupAdapter() {
        adapter = object : ArrayAdapter<ResolveInfo>(
            context,
            R.layout.app_list_item,
            R.id.open_app_button,
            apps
        ) {

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val app = getItem(position) ?: return view

                val pkg = app.activityInfo.packageName

                val label = view.findViewById<TextView>(R.id.open_app_button)
                val emptySpace = view.findViewById<View>(R.id.empty_space)
                val actions = view.findViewById<View>(R.id.actions_container)

                val favButton = view.findViewById<View>(R.id.favorite_button)
                val favIcon = view.findViewById<ImageView>(R.id.favorite_icon)
                val closeButton = view.findViewById<View>(R.id.close_button)
                val bottomBorder =
                    view.findViewById<View>(R.id.favorite_bottom_border)
                val settingsButton =
                    view.findViewById<View>(R.id.settings_button)

                // ----------------------------------------------------------------
                // Bind
                // ----------------------------------------------------------------

                label.text = app.loadLabel(pm)

                favIcon.isSelected = isFavorite(pkg)

                actions.visibility =
                    if (openActionsFor == pkg) View.VISIBLE else View.GONE

                bottomBorder.visibility =
                    if (isLastFavorite(position)) View.VISIBLE else View.GONE

                // ----------------------------------------------------------------
                // Clicks
                // ----------------------------------------------------------------

                label.setOnClickListener { launchApp(pkg) }
                emptySpace.setOnClickListener { launchApp(pkg) }

                label.setOnLongClickListener {
                    openRowActions(pkg)
                    true
                }

                emptySpace.setOnLongClickListener {
                    context.startActivity(
                        Intent(context, SettingsActivity::class.java)
                    )
                    true
                }

                settingsButton.setOnClickListener {
                    openAppSettings(pkg)
                }

                favButton.setOnClickListener {
                    val newState = !favIcon.isSelected
                    favIcon.isSelected = newState
                    setFavorite(pkg, newState)
                    refresh()
                }

                closeButton.setOnClickListener {
                    closeRowActions()
                }

                return view
            }

            private fun isLastFavorite(position: Int): Boolean {
                val current = getItem(position) ?: return false
                val pkg = current.activityInfo.packageName

                if (!isFavorite(pkg)) return false

                val next = apps.getOrNull(position + 1)
                return next == null ||
                        !isFavorite(next.activityInfo.packageName)
            }
        }

        listView.adapter = adapter
    }

    // ------------------------------------------------------------------------
    // Scroll behavior
    // ------------------------------------------------------------------------

    private fun setupScrollListener() {
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {

            override fun onScrollStateChanged(
                view: AbsListView?,
                scrollState: Int
            ) {
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE &&
                    openActionsFor != null
                ) {
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
