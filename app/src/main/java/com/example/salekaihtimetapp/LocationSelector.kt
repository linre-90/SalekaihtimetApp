package com.example.salekaihtimetapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource


@OptIn(ExperimentalPermissionsApi::class)
@Composable
/**
 * Location query button. Handles permissions.
 * */
fun LocationSelector(setLatitude: (Float) -> Unit, setLongitude: (Float) -> Unit){
    val locationPermission = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    var findingLocation by remember { mutableStateOf(false) }
    var locationMsg by remember { mutableStateOf("") }
    var firstLocation by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if(locationPermission.allPermissionsGranted && firstLocation){
        // Start updating location if permissions are already granted.
        queryLocation(
            context =  context,
            onFindingLocation = { newVal -> findingLocation = newVal },
            setLocatingStatus = {newVal -> locationMsg = newVal},
            updateLatitude = {lat -> setLatitude(lat)},
            updateLongitude = {lat -> setLongitude(lat)},
        )
        firstLocation = false
    }


    // Button to start user locating
    Button(
        enabled = !findingLocation,
        modifier =  Modifier.padding(0.dp, 20.dp),
        onClick = {
            if (!locationPermission.allPermissionsGranted) {
                locationPermission.launchMultiplePermissionRequest()
            } else {
                // Manually query location when btn is pressed.
                queryLocation(
                    context =  context,
                    onFindingLocation = { newVal -> findingLocation = newVal },
                    setLocatingStatus = {newVal -> locationMsg = newVal},
                    updateLatitude = {lat -> setLatitude(lat)},
                    updateLongitude = {lat -> setLongitude(lat)},
                )
            }
    }) {
        if(locationMsg.isNotEmpty() && !findingLocation){
            Text(text=locationMsg)
        }else if(findingLocation){
            Text("...Getting location...")
        } else{
            Text("Find location")
        }
    }
}

/**
 * Find users current latitude and longitude.
 * */
@SuppressLint("MissingPermission")
fun queryLocation(
    context: Context,
    onFindingLocation: (Boolean) -> Unit,
    setLocatingStatus: (String) -> Unit,
    updateLongitude: (Float) -> Unit,
    updateLatitude: (Float) ->Unit
){
    onFindingLocation(true)
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation( Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener {
            if(it != null){
                setLocatingStatus("Location found, click to retry.")
                updateLongitude(it.longitude.toFloat())
                updateLatitude(it.latitude.toFloat())
                onFindingLocation(false)
            }
        }.addOnCanceledListener {
            setLocatingStatus("Location search cancelled.")
            onFindingLocation(false)
        }
}

