package com.github.myapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.myapplication.InAppUpdateUtils.Companion.REQUEST_CODE_FLEXIBLE_UPDATE
import com.google.android.play.core.install.model.InstallStatus
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var inAppUpdateUtils: InAppUpdateUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inAppUpdateUtils = InAppUpdateUtils(this, ::onStateUpdateChange)
        inAppUpdateUtils.initAppUpdaterAndCheckForUpdate(llDownloadUpdate)

        /**
         * If update is available then start update otherwise show install update button
         */
        btnDownloadInstall.setOnClickListener {
            when (btnDownloadInstall.text) {
                getString(R.string.update) -> inAppUpdateUtils.startForInAppUpdate()
                getString(R.string.install) -> inAppUpdateUtils.completeUpdate()
            }
        }
        // Hide the In-app-update UI if user selects later
        btnLater.setOnClickListener { llDownloadUpdate.visibility = View.GONE }

    }

    override fun onResume() {
        super.onResume()
        // Check all update is already downloaded or not if then show install update ui only
        inAppUpdateUtils.ifUpdateDownloadedThenInstall()
    }

    // As the In-App-Update status changes update the UI to show Download, Install or later button
    private fun onStateUpdateChange(installStatus: Int) {
        when (installStatus) {
            InstallStatus.PENDING -> {
                llDownloadUpdate.visibility = View.VISIBLE
            }
            InstallStatus.DOWNLOADED -> {
                btnDownloadInstall.text = getString(R.string.install)
                tvUpdateAvailable.text = getString(R.string.app_update_downloaded)
                llDownloadUpdate.visibility = View.VISIBLE
                llUpdateAction.visibility = View.VISIBLE
                llUpdateDownloadProgress.visibility = View.GONE
            }
            InstallStatus.DOWNLOADING -> {
                llDownloadUpdate.visibility = View.VISIBLE
                llUpdateAction.visibility = View.GONE
                llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLING -> {
                tvUpdateProgress.text = getString(R.string.installing_update)
                llDownloadUpdate.visibility = View.GONE
                llUpdateAction.visibility = View.GONE
                llUpdateDownloadProgress.visibility = View.VISIBLE
            }
            InstallStatus.INSTALLED, InstallStatus.UNKNOWN -> {
                llDownloadUpdate.visibility = View.GONE
            }
            else -> {
                llDownloadUpdate.visibility = View.GONE
            }
        }
    }

    // If user ignore the update then re-check update as user may want to install the update later
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXIBLE_UPDATE
            && resultCode != Activity.RESULT_OK
        ) {
            inAppUpdateUtils.checkUpdateAvailable(llDownloadUpdate)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateUtils.unregisterListener()
    }
}
