package com.example.salekaihtimetapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationSelector(setLatitude: (Float) -> Unit, setLongitude: (Float) -> Unit){
    val locationPermission = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val showRationalDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current
    Button(
        modifier =  Modifier.padding(0.dp, 20.dp),
        onClick = {
            if (!locationPermission.allPermissionsGranted) {
                if (locationPermission.shouldShowRationale) {
                    // Show a rationale if needed (optional)
                    showRationalDialog.value = true
                } else {
                    // Request the permission
                    locationPermission.launchMultiplePermissionRequest()
                }
            } else {
                Log.d("PERMISSION_LOC", "Already granted")
                getLocation(context)
            }
    }) {
        Text("Find location")
    }
}

@SuppressLint("MissingPermission")
fun getLocation(context: Context){
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener {
        Log.d("PERMISSION_LOC", (it == null).toString())
    }.addOnCanceledListener {

    }



//    fusedLocationClient.lastLocation.addOnSuccessListener {
//        location -> Log.d("PERMISSION_LOC", (location == null).toString())
//    }

}

