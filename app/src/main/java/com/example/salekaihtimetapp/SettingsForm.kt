package com.example.salekaihtimetapp

import android.icu.util.TimeZone
import android.util.Log
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
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign


@RequiresApi(Build.VERSION_CODES.S)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsForm(){
    // Create states
    val timePickerState by remember { mutableStateOf(TimePickerState(8, 0, is24Hour = true)) }
    var loading by remember { mutableStateOf(false) }
    var durationState by remember{ mutableStateOf("8") }
    var latitudeState by remember { mutableFloatStateOf(63.096F) }
    var longitudeState by remember { mutableFloatStateOf(21.61577F) }

    // Render form if not loading
    if(!loading){
        Column (
            Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())){
            FormText(name = "Find your location", hint = "Use phone gps to get location.")
            LocationSelector(
                setLatitude = {value -> latitudeState = value},
                setLongitude = {value -> longitudeState = value}
            )
            Divider()
            FormText(name = "Force close time", hint = "Forces curtain to close")
            TimePicker(state = timePickerState, Modifier.padding(0.dp, 20.dp))
            Divider()
            FormText(name = "Close time duration", hint = "Force close duration." )
            TextField(
                modifier = Modifier.padding(0.dp, 20.dp),
                value=durationState,
                onValueChange = { newVal -> durationState = newVal},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Divider()
            FormText(name = "Upload settings to curtains.", hint = "Before submitting make sure you are connected via bluetooth.")
            Button(
                modifier = Modifier.padding(0.dp, 20.dp),
                onClick = {
                    // Submit form to backend server.
                    val userSettings = UserSettings(
                        latitudeState.toString(),
                        longitudeState.toString(),
                        timePickerState.hour.toString(),
                        timePickerState.minute.toString(),
                        durationState
                    )

                    handleSettingsSubmit({ isLoading -> loading = isLoading }, userSettings)
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
        Text("...Uploading...", fontSize = 24.sp, textAlign = TextAlign.Center)
    }
}

private fun handleSettingsSubmit(onSetLoading: (Boolean) -> Unit, userSettings: UserSettings){
    onSetLoading(true)
    // year, month, day does not matter... These are in local time
    val openLDT = LocalDateTime.of(1980, 7, 10, userSettings.closeTimeHour.toInt(), userSettings.closeTimeMinute.toInt(), 0)

    // Convert local times to utc
    val closeLZDT = ZonedDateTime.of(openLDT, ZoneId.of(TimeZone.getDefault().id))
    val closeUTC = closeLZDT.withZoneSameInstant(ZoneId.of(TimeZone.GMT_ZONE.id))

    // Server expects following: close_start=22:00;close_duration=8;latitude=63.096;longitude=21.61577;
    val data = "close_start=${closeUTC.hour}:${closeUTC.minute};close_duration=${userSettings.closeDuration};latitude=${userSettings.latitude};longitude=${userSettings.longitude};"
    Log.d("USER_DATA", data)
    onSetLoading(false)
}