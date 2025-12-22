package com.rama.mako

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.about_view)

        val root = findViewById<View>(android.R.id.content)
        root.setOnApplyWindowInsetsListener { v, insets ->
            val topInset = insets.systemWindowInsetTop
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        val repoButton = findViewById<LinearLayout>(R.id.repo_button)
        repoButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/jmiguelrivas/mako")
            )
            startActivity(intent)
        }

        val creatorButton = findViewById<LinearLayout>(R.id.creator_button)
        creatorButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/jmiguelrivas")
            )
            startActivity(intent)
        }
    }
}