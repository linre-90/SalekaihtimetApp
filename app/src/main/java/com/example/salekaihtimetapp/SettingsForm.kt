package com.example.salekaihtimetapp

import android.icu.util.TimeZone
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.net.ConnectException


const val CLIENT_IP = "10.42.0.1" // Raspberry
const val CLIENT_PORT = 8080

@RequiresApi(Build.VERSION_CODES.S)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
/**
 * Render user settings form.
 * */
fun SettingsForm(){
    // Create states
    val timePickerState by remember { mutableStateOf(TimePickerState(22, 0, is24Hour = true)) }
    var loading by remember { mutableStateOf(false) }
    var durationState by remember{ mutableStateOf("8") }
    var latitudeState by remember { mutableFloatStateOf(63.096F) }
    var longitudeState by remember { mutableFloatStateOf(21.61577F) }
    var deviceResponse by remember { mutableStateOf<DeviceResponse?>(null) }
    val context = LocalContext.current

    if(deviceResponse != null){
        Toast.makeText(context, deviceResponse!!.msg, Toast.LENGTH_LONG).show()
        deviceResponse = null
    }

    // Render form if not loading
    if(!loading){
        Column (
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())){
            // User locating
            FormText(name = "Find your location", hint = "Use phone gps to get location.")
            LocationSelector(
                setLatitude = {value -> latitudeState = value},
                setLongitude = {value -> longitudeState = value}
            )
            Divider()

            // Force closing time hour:min input
            FormText(name = "Force close time", hint = "Forces curtain to close")
            TimePicker(state = timePickerState, Modifier.padding(0.dp, 20.dp))
            Divider()

            // Closing time duration
            FormText(name = "Close time duration", hint = "Force close duration. Value must be from 1 to 23 and is measured in hours." )
            TextField(
                modifier = Modifier.padding(0.dp, 20.dp),
                value=durationState,
                onValueChange = { newVal -> durationState = newVal },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Divider()

            // Upload button
            FormText(name = "Upload settings to curtains.", hint = "Before submitting make sure you are connected via wifi.")
            Button(
                modifier = Modifier.padding(0.dp, 20.dp),
                onClick = {
                    // Check duration
                    val validDuration = validateDuration(durationState)
                    if(!validDuration){
                        Toast.makeText(context, "Duration value is invalid. Duration must be integer from 1 to 23.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Submit form to backend server.
                    val userSettings = UserSettings(
                        latitudeState.toString(),
                        longitudeState.toString(),
                        timePickerState.hour.toString(),
                        timePickerState.minute.toString(),
                        durationState
                    )
                    // Make post request
                    val scope = MainScope()
                    scope.launch(Dispatchers.IO) {
                        loading = true
                        val res = handleSettingsSubmit(userSettings)
                        deviceResponse = res
                        loading = false
                    }
            }) { Text(text = "Upload") }
        }
    }else{
        // Loading indicator
        Loading()
    }
}

@Composable
private fun FormText(name: String, hint: String){
    Column {
        Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier =  Modifier.padding(0.dp, 20.dp))
        Text(text = hint, fontSize = 12.sp, fontStyle = FontStyle.Italic)
    }
}

@Composable
private fun Loading(){
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Upload user settings to device.
 * Sends new settings with post request.
 * Device is set back to operation mode, closing upload mode and web server.
 * */
private suspend fun handleSettingsSubmit(userSettings: UserSettings): DeviceResponse{
    val client = HttpClient()
    var dResponse = DeviceResponse("New settings uploaded to device.", HttpStatusCode.OK )

    // year, month, day does not matter... These are in local time
    val openLDT = LocalDateTime.of(1980, 7, 10, userSettings.closeTimeHour.toInt(), userSettings.closeTimeMinute.toInt(), 0)

    // Convert local times to utc
    val closeLZDT = ZonedDateTime.of(openLDT, ZoneId.of(TimeZone.getDefault().id))
    val closeUTC = closeLZDT.withZoneSameInstant(ZoneId.of(TimeZone.GMT_ZONE.id))

    // Server expects following: close_start=22:00;close_duration=8;latitude=63.096;longitude=21.61577;
    val data = "close_start=${closeUTC.hour}:${closeUTC.minute};close_duration=${userSettings.closeDuration};latitude=${userSettings.latitude};longitude=${userSettings.longitude};"

    // Do the request
    try {
        val response: HttpResponse = client.post("http://$CLIENT_IP:$CLIENT_PORT"){
            setBody(data)
            contentType(ContentType.Text.Plain)
        }
        if(response.status != HttpStatusCode.OK){
            dResponse = DeviceResponse.buildBadRequestResponse()
        }

    }catch (e: ConnectTimeoutException){
        dResponse = DeviceResponse.buildTimeoutResponse()
    }catch (ref: ConnectException){
        dResponse = DeviceResponse(ref.message.toString(), HttpStatusCode.NotFound)
    }
    finally {
        client.close()
    }

    return dResponse
}

/**
 * Enforce duration to be in range of 1-23.
 * */
private fun validateDuration(duration: String): Boolean{
    return try {
        val d = duration.toInt()
        d in 1..23
    }catch (e: NumberFormatException){
        false
    }
}