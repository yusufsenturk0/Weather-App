package com.example.wetherapp

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object RateManager {
    private const val PREFS_NAME = "app_rater"
    private const val KEY_LAUNCH_COUNT = "launch_count"
    private const val KEY_NEVER_SHOW = "never_show"
    private const val KEY_LAST_SHOWN_COUNT = "last_shown_count"

    private const val LAUNCHES_UNTIL_PROMPT = 5
    private const val LAUNCHES_UNTIL_NEXT_PROMPT = 15

    fun checkRateApp(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_NEVER_SHOW, false)) {
            return
        }

        val editor = prefs.edit()
        val launchCount = prefs.getLong(KEY_LAUNCH_COUNT, 0) + 1
        editor.putLong(KEY_LAUNCH_COUNT, launchCount)

        val lastShownCount = prefs.getLong(KEY_LAST_SHOWN_COUNT, 0)

        var shouldShow = false
        
        if (launchCount == LAUNCHES_UNTIL_PROMPT.toLong()) {
            shouldShow = true
        } else if (launchCount > LAUNCHES_UNTIL_PROMPT.toLong()) {
             if (launchCount - lastShownCount >= LAUNCHES_UNTIL_NEXT_PROMPT) {
                 shouldShow = true
             }
        }

        if (shouldShow) {
            // Delay for 5 seconds to let the app load first
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showCustomRateDialog(context, editor, launchCount)
            }, 5000)
        }
        
        editor.apply()
    }

    private fun showCustomRateDialog(context: Context, editor: android.content.SharedPreferences.Editor, currentLaunchCount: Long) {
        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_rate_me, null)
        
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setCancelable(false) // Prevent closing by clicking outside
        val dialog = builder.create()
        
        // Transparent background for rounded corners
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        val btnRateNow = dialogView.findViewById<android.view.View>(R.id.btnRateNow)
        val btnLater = dialogView.findViewById<android.view.View>(R.id.btnLater)
        val tvNoThanks = dialogView.findViewById<android.view.View>(R.id.tvNoThanks)

        btnRateNow.setOnClickListener {
            val packageName = context.packageName
            val uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                context.startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
            
            editor.putBoolean(KEY_NEVER_SHOW, true)
            editor.apply()
            dialog.dismiss()
        }

        btnLater.setOnClickListener {
            editor.putLong(KEY_LAST_SHOWN_COUNT, currentLaunchCount)
            editor.apply()
            dialog.dismiss()
        }

        tvNoThanks.setOnClickListener {
            editor.putBoolean(KEY_NEVER_SHOW, true)
            editor.apply()
            dialog.dismiss()
        }

        dialog.show()
    }
}
