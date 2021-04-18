package com.example.kotlin_app.data

data class LoginReq(
    var email: String = "",
    var password: String = "",
)

data class LoginRes(
    var token: String = "",
    var message: String = "",
)
