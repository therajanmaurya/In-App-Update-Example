package com.github.myapplication

import android.app.Activity
import android.view.View
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 *  In-App-Update Utils class
 */
class InAppUpdateUtils constructor(
    private val context: Activity,
    private val onStateUpdateChange: (Int) -> Unit
) : InstallStateUpdatedListener {

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo

    /**
     * Initialize AppUpdateManager and check update
     */
    fun initAppUpdaterAndCheckForUpdate(updateView: View) {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        registerListener()
        checkUpdateAvailable(updateView)
    }

    /**
     * Check Update is available or not
     */
    fun checkUpdateAvailable(updateView: View) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateInfo = it
                updateView.visibility = View.VISIBLE
            } else {
                updateView.visibility = View.GONE
                unregisterListener()
            }
        }
    }

    /**
     * As In-App-Update status changes It is being called
     */
    override fun onStateUpdate(state: InstallState) {
        onStateUpdateChange.invoke(state.installStatus())
    }

    // If the update is downloaded but not installed,
    // notify the user to complete the update.
    fun ifUpdateDownloadedThenInstall() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onStateUpdateChange.invoke(appUpdateInfo.installStatus())
                }
            }
    }

    /**
     * Start downloading updates
     */
    fun startForInAppUpdate() {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            context,
            REQUEST_CODE_FLEXIBLE_UPDATE
        )
    }

    /**
     * Install the update
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    private fun registerListener() = appUpdateManager.registerListener(this)

    fun unregisterListener() = appUpdateManager.unregisterListener(this)

    companion object {
        const val REQUEST_CODE_FLEXIBLE_UPDATE = 17362
    }
}
