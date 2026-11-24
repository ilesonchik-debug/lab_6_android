package com.example.main_dd

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main_dd.ui.theme.Main_ddTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class avtorizator : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            Main_ddTheme {

                AuthorizationScreen()
            }
        }
    }
}


@Composable
fun AuthorizationScreen() {

    // ========================================
    // КОНТЕКСТ ПРИЛОЖЕНИЯ
    // ========================================

    val context = LocalContext.current


    // ========================================
    // ПЕРЕМЕННАЯ ДЛЯ ЛОГИНА
    // ========================================

    var login by remember {

        mutableStateOf("")
    }


    // ========================================
    // ПЕРЕМЕННАЯ ДЛЯ ПАРОЛЯ
    // ========================================

    var password by remember {

        mutableStateOf("")
    }


    // ========================================
    // ОКНО ОШИБКИ
    // ========================================

    var errorMessage by remember {

        mutableStateOf<String?>(null)
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ========================================
        // ФОНОВАЯ КАРТИНКА
        // ========================================

        Image(
            painter = painterResource(
                id = R.drawable.greenfon
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // ========================================
        // INPUT ПОЛЯ
        // ========================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            // ====================================
            // ЛОГИН
            // ====================================

            OutlinedTextField(
                value = login,

                onValueChange = {

                    login = it
                },

                placeholder = {

                    Text(
                        text = "имя",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                },

                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                ),

                singleLine = true,

                colors = TextFieldDefaults.colors(

                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    focusedContainerColor =
                        Color(0xAA008F8F),

                    unfocusedContainerColor =
                        Color(0xAA008F8F),

                    focusedIndicatorColor =
                        Color.White,

                    unfocusedIndicatorColor =
                        Color.White,

                    focusedPlaceholderColor =
                        Color.White,

                    unfocusedPlaceholderColor =
                        Color.White,

                    cursorColor =
                        Color.White
                ),

                shape = RoundedCornerShape(18.dp),

                modifier = Modifier
                    .width(330.dp)
                    .height(72.dp)
            )


            // ====================================
            // ПАРОЛЬ
            // ====================================

            OutlinedTextField(
                value = password,

                onValueChange = {

                    password = it
                },

                placeholder = {

                    Text(
                        text = "пароль",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                },

                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                ),

                singleLine = true,

                colors = TextFieldDefaults.colors(

                    focusedTextColor =
                        Color.White,

                    unfocusedTextColor =
                        Color.White,

                    focusedContainerColor =
                        Color(0xAA008F8F),

                    unfocusedContainerColor =
                        Color(0xAA008F8F),

                    focusedIndicatorColor =
                        Color.White,

                    unfocusedIndicatorColor =
                        Color.White,

                    focusedPlaceholderColor =
                        Color.White,

                    unfocusedPlaceholderColor =
                        Color.White,

                    cursorColor =
                        Color.White
                ),

                shape = RoundedCornerShape(18.dp),

                modifier = Modifier
                    .width(330.dp)
                    .height(72.dp)
                    .padding(top = 10.dp)
            )
        }


        // ========================================
        // КНОПКА ВХОДА
        // ========================================

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .width(280.dp)
                .height(50.dp)
                .clip(
                    RoundedCornerShape(8.dp)
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF006666),
                            Color(0xFF00B8B8)
                        )
                    )
                )
        ) {

            Button(
                onClick = {

                    // ==================================
                    // ЗАПУСКАЕМ АВТОРИЗАЦИЮ
                    // ==================================

                    CoroutineScope(
                        Dispatchers.Main
                    ).launch {

                        authorize(
                            login = login,
                            password = password
                        ) { result ->

                            // ==================================
                            // ОБРАБАТЫВАЕМ РЕЗУЛЬТАТ
                            // ==================================

                            when (result) {

                                // ==================================
                                // АВТОРИЗАЦИЯ УСПЕШНА
                                // ==================================

                                AuthResult.Success -> {

                                    val intent =
                                        Intent(
                                            context,
                                            Hub::class.java
                                        )


                                    // ==================================
                                    // УДАЛЯЕМ АВТОРИЗАЦИЮ ИЗ СТЕКА
                                    //
                                    // ПОСЛЕ ПЕРЕХОДА:
                                    //
                                    // Hub
                                    //
                                    // А НЕ:
                                    //
                                    // avtorizator
                                    //     ↓
                                    // Hub
                                    // ==================================

                                    intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    )


                                    context.startActivity(
                                        intent
                                    )
                                }


                                // ==================================
                                // НЕВЕРНЫЙ ЛОГИН ИЛИ ПАРОЛЬ
                                // ==================================

                                AuthResult.WrongLoginPassword -> {

                                    errorMessage =
                                        "неверное имя или пароль"
                                }


                                // ==================================
                                // ВЕРСИЯ ПРИЛОЖЕНИЯ УСТАРЕЛА
                                // ==================================

                                AuthResult.AppOutdated -> {

                                    errorMessage =
                                        "версия приложения устарела"
                                }
                            }
                        }
                    }
                },

                modifier =
                    Modifier.fillMaxSize(),

                shape =
                    RoundedCornerShape(8.dp),

                colors =
                    androidx.compose.material3
                        .ButtonDefaults
                        .buttonColors(
                            containerColor =
                                Color.Transparent
                        )
            ) {

                Text(
                    text = "войти",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }


        // ========================================
        // МИНИ ОКНО ОШИБКИ
        // ========================================

        if (errorMessage != null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0x66000000)
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(
                            RoundedCornerShape(18.dp)
                        )
                        .background(
                            Color(0xFF202020)
                        )
                        .padding(
                            horizontal = 20.dp,
                            vertical = 20.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    // ==================================
                    // ТЕКСТ ОШИБКИ
                    // ==================================

                    Text(
                        text =
                            errorMessage ?: "",

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        textAlign =
                            TextAlign.Center,

                        modifier =
                            Modifier.fillMaxWidth()
                    )


                    // ==================================
                    // ОТСТУП
                    // ==================================

                    androidx.compose.foundation.layout.Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )


                    // ==================================
                    // КНОПКА ЗАКРЫТИЯ
                    // ==================================

                    Button(
                        onClick = {

                            errorMessage = null
                        },

                        modifier = Modifier
                            .width(150.dp)
                            .height(45.dp),

                        shape =
                            RoundedCornerShape(10.dp),

                        colors =
                            androidx.compose.material3
                                .ButtonDefaults
                                .buttonColors(
                                    containerColor =
                                        Color(0xFF006666)
                                )
                    ) {

                        Text(
                            text = "закрыть",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    showBackground = true
)
@Composable
fun AuthorizationPreview() {

    Main_ddTheme {

        AuthorizationScreen()
    }
}