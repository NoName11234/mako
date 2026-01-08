package com.rama.mako

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ListView

class MainActivity : Activity() {
    private lateinit var listView: ListView
    private lateinit var appListHelper: AppListHelper

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_home)

        val root = findViewById<View>(R.id.root)
        root.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft + dp(16),
                insets.systemWindowInsetTop + dp(16),
                insets.systemWindowInsetRight + dp(16),
                insets.systemWindowInsetBottom + dp(16)
            )
            insets
        }

        listView = findViewById(R.id.appList)

        appListHelper = AppListHelper(this, listView)
        appListHelper.setup()
    }

    override fun onResume() {
        super.onResume()
        appListHelper.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
