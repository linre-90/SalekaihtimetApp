package com.example.salekaihtimetapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Welcome(navController: NavHostController){
    Column (modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){
        Text("Automatic curtains", fontSize = 25.sp, modifier = Modifier.padding(0.dp, 30.dp))
        Button(onClick = { navController.navigate("quickstart") }) {
            Text(text = "Quick start guide")
        }
        Button(onClick = { navController.navigate("create_settings") }) {
            Text(text = "Setup settings")
        }
    }
}