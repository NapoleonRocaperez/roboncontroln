package com.example.roboncontroln

import android.Manifest

import android.os.Build

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.roboncontroln.utils.AdministrardorBluetooh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import kotlinx.coroutines.launch


import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import kotlin.math.*



@Composable
fun RobotControllerScreen(onNavigateToAbout: ()-> Unit){

    val context= LocalContext.current
    val coroutineScope= rememberCoroutineScope()
    val bluetoothManager= remember { AdministrardorBluetooh(context) }

    var conectado by remember { mutableStateOf(false) }
    var conectando by remember { mutableStateOf(false) }
    var weaponState by remember { mutableStateOf(0) }
    var joystickX by remember { mutableStateOf(0) }
    var joystickY by remember { mutableStateOf(0) }


    //SISTEMA DE PERMISOS
    val  launcherPermisos= rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        permisos ->
        val permisosAceptados=permisos.entries.all { it.value }
        if(!permisosAceptados){
            Toast.makeText(context,"Debes dar permiso  Bluetooth para controlar el robot", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            launcherPermisos.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Box(
            modifier= Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment =  Alignment.Center


        ){
            Image(
                painter = painterResource(id = R.drawable.logo_robot2 ),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable { onNavigateToAbout() }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
           //Joystick
            VirtualJoystick(onMove = { x, y ->
                joystickX = x
                joystickY = y
                if (conectado)bluetoothManager.enviarComando(joystickX,joystickY ,weaponState)
            })

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        if (!conectado) {
                            conectando = true
                            coroutineScope.launch {
                                conectado = bluetoothManager.conectar()
                                conectando = false
                            }
                        } else {
                            bluetoothManager.desconectar()
                            conectado = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if(conectado) Color.Red else Color(0xFF4CAF50))
                ) {
                    Text (if(conectando)"...." else if (conectado)"OFF" else "CONECTAR")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("X:$joystickX | Y:$joystickY", color=Color.Cyan, fontSize = 14.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Button(
                    onClick = {weaponState=1; if (conectado) bluetoothManager.enviarComando(joystickX,joystickY,weaponState)},
                    modifier = Modifier.size(80.dp), shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ){Text("A", fontSize = 24.sp, fontWeight = FontWeight.Bold)}

                Button(
                    onClick = {weaponState=0; if (conectado) bluetoothManager.enviarComando(joystickX,joystickY,weaponState)},
                    modifier = Modifier.size(80.dp), shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ){Text("B", fontSize = 24.sp, fontWeight = FontWeight.Bold)}
            }

        }

    }

}

@Composable
fun VirtualJoystick(onMove:(Int, Int)-> Unit) {
    val maxRadius=with(LocalDensity.current){70.dp.toPx()}
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.size(160.dp).clip(CircleShape).background(Color(0xFF333333)),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .offset{ IntOffset(offsetX.roundToInt(),offsetY.roundToInt()) }
                .size(55.dp).clip(CircleShape).background(Color(0xFF03A9F4))
                .pointerInput(Unit){
                  detectDragGestures ( onDragEnd = { offsetX=0f;offsetY=0f;onMove(0,0) }){change, dragAmount ->
                      change.consume()
                      val newX=offsetX+dragAmount.x
                      val newY=offsetY+dragAmount.y
                      val distance=hypot(newX.toDouble(),  newY.toDouble())
                      if(distance <=maxRadius){offsetX=newX; offsetY=newY}
                      else{
                          val angle=atan2(newY.toDouble(),newX.toDouble())
                          offsetX=(cos(angle)*maxRadius).toFloat()
                          offsetY=(sin(angle)*maxRadius).toFloat()
                      }
                      onMove(((offsetX/maxRadius)*255).toInt(),(-(offsetY/maxRadius)*255).toInt())

                  }
                }
        )
    }

}