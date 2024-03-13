package com.example.salekaihtimetapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickStart(){
    Box (modifier = Modifier.padding(10.dp)){

        Column (modifier = Modifier.padding(10.dp)){
            Text(text = "Quick start", fontSize = 40.sp, modifier = Modifier.padding(0.dp, 0.dp, 20.dp,20.dp))
            Divider()
            Text("1. Turn on pairing mode on curtain device", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("2. On your mobile phone browse available wifi networks and select network named curtain-xxxxxx. X is serial number printed to physical manual or to curtain control box.", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("3. Open 'SalekaihtimetApp' and press 'Setup settings' button.", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("4. Fill available fields and press 'upload' when ready. Pay attention to pop up messages, they might contain error.", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("5. Select desired operation mode from curtain control box.", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("6. Setup is complete", modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp))
            Text("Important: Pressing upload or cancel button will cause curtain device to stop accepting communication, in case of any error messages restart curtain device and start quick start procedure again.", fontStyle = FontStyle.Italic, modifier = Modifier.padding(20.dp, 10.dp, 10.dp, 10.dp))
        }
    }
}