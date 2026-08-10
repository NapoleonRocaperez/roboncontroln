package com.example.roboncontroln.AppNavGraph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.roboncontroln.RobotControllerScreen

import com.example.roboncontroln.screens.AboutScreen


@Composable
fun AppNavGraph(){
    val navController = rememberNavController()

    NavHost(navController=navController, startDestination = "control"){
        //pantalla de control del robot

        composable("control"){
            RobotControllerScreen(
                onNavigateToAbout={navController.navigate("about")}
            )
        }

        //pantalla Acerca de
        composable("about"){
            AboutScreen(
                onBack={navController.popBackStack()}
            )
        }
    }

}