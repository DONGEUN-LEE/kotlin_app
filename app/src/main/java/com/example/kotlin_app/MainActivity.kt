package com.example.kotlin_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore
import com.example.kotlin_app.api.MainAPI
import com.example.kotlin_app.component.LoadingDialog
import com.example.kotlin_app.data.LoginReq
import com.example.kotlin_app.data.LoginRes
import com.example.kotlin_app.data.UserPreferencesRepository
import com.example.kotlin_app.ui.theme.Kotlin_appTheme
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        // Since we're migrating from SharedPreferences, add a migration based on the
        // SharedPreferences name
        listOf(SharedPreferencesMigration(context, USER_PREFERENCES_NAME))
    }
)

private lateinit var userPreferencesRepository: UserPreferencesRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        GlobalScope.launch(Dispatchers.Main) {
            if (::userPreferencesRepository.isInitialized) {
                userPreferencesRepository.getToken{
                    var loginState = false
                    if (!it.isNullOrEmpty()) {
                        loginState = true
                    }
                    setContent {
                        Kotlin_appTheme {
                            // A surface container using the 'background' color from the theme
                            Surface(color = MaterialTheme.colors.background) {
                                Content(loginState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Content(loginState: Boolean = false) {
    Scaffold {
        val isLogedin = remember { mutableStateOf(loginState) }
        if (!isLogedin.value) {
            Login(isLogedin)
        } else {
            Main(isLogedin)
        }
    }
}

@Composable
fun Login(
    isLogedin: MutableState<Boolean>,
) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val email = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val loadingState = remember { mutableStateOf(false) }
        TextField(
            modifier = Modifier.padding(5.dp),
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") }
        )
        TextField(
            modifier = Modifier.padding(5.dp),
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )
        Button(
            onClick = {
                loadingState.value = true
                val url = BuildConfig.API_SERVER_URL
                val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(MainAPI::class.java)
                val callLogin = api.login(LoginReq(
                    email.value.text,
                    password.value.text,
                ))

                callLogin.enqueue(object : Callback<LoginRes> {
                    override fun onResponse(
                        call: Call<LoginRes>,
                        response: Response<LoginRes>
                    ) {
                        GlobalScope.launch {
                            launch {
                                if (::userPreferencesRepository.isInitialized) {
                                    userPreferencesRepository.updateToken(response.body()?.token)
                                }
                            }
                            loadingState.value = false
                            isLogedin.value = true
                        }
                    }

                    override fun onFailure(call: Call<LoginRes>, t: Throwable) {
                        loadingState.value = false
                    }
                })
            },
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.surface,
            )
        ) {
            Text("Sign In")
        }
        LoadingDialog(isDisplayed = loadingState.value)
    }
}

@Composable
fun Main(
    isLogedin: MutableState<Boolean>,
) {
    TopAppBar(
        title = { Text(text = "Title") },
        navigationIcon = {
            IconButton(onClick = { }) {
                // below line is use to
                // specify navigation icon.
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                )
            }
        },
        actions = {
            IconButton(onClick = {
                GlobalScope.launch {
                    launch {
                        if (::userPreferencesRepository.isInitialized) {
                            userPreferencesRepository.removeToken()
                        }
                    }
                    isLogedin.value = false
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                )
            }
        }
    )
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Welcome!")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Kotlin_appTheme {
        Content()
    }
}