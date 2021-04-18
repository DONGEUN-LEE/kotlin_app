package com.example.kotlin_app.component

import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun LoadingDialog(
    isDisplayed: Boolean
){
    if (isDisplayed) {
        AlertDialog(
            onDismissRequest = {},
            text = {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.primary
                )
            },
            buttons = {}
        )
    }
}