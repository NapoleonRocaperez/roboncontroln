package com.example.roboncontroln.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.icu.util.Output
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


@SuppressLint("MissingPermission")
class AdministrardorBluetooh (context: Context){

    private val bluetoothAdapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    private var socket : BluetoothSocket?=null
    private var outputStream: OutputStream?=null

    private val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private val NOMBRE_ROBOT="ROBOT_COMPETENCIA"


    suspend fun conectar(): Boolean= withContext(Dispatchers.IO){
        if(bluetoothAdapter== null || !bluetoothAdapter.isEnabled) return@withContext false

        val dispositivosVinculados: Set<BluetoothDevice>?= bluetoothAdapter.bondedDevices
        val robotDevice = dispositivosVinculados?.find { it.name==NOMBRE_ROBOT }?: return@withContext  false

        return@withContext try {
            socket=robotDevice.createRfcommSocketToServiceRecord(UUID_SPP)
            bluetoothAdapter.cancelDiscovery()
            socket?.connect()
            outputStream=socket?.outputStream
            true
        }catch (e: IOException){
            desconectar()
            false
        }
    }

    fun enviarComando(x: Int, y: Int, arma:Int){
        if (socket?.isConnected==true && outputStream !=null){
            try {
                val paquete= "$x,$y,$arma\n"
                outputStream?.write(paquete.toByteArray())
                outputStream?.flush()
            }catch (e: IOException){
                Log.e("BLUETOOTH","Error al ENVIAR: ${e.message}")
            }
        }
    }

    fun desconectar (){
        try {
            outputStream?.close()
            socket?.close()
            socket=null
            outputStream=null

        }catch (e: IOException){
            Log.e("BLUETOOTH","Error al desconectar: ${e.message}")
        }
    }


}