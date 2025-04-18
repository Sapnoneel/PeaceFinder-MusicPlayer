package com.example.peacefinder.utils

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

object PermissionUtils {
    @OptIn(ExperimentalPermissionsApi::class)
    suspend fun requestPermission(permissionState: PermissionState) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
}
