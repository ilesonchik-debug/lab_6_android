package com.example.main_dd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit



// ==========================================
// НАСТРОЙКИ АВТОРИЗАЦИИ
// ==========================================

private const val SERVER_URL = "ws://217.14.204.56:8222"

private const val SERVER_KEY = "ebin410"


// ==========================================
// РАЗМЕРЫ ПАКЕТА
// ==========================================

private const val KEY_SIZE = 7
private const val LOGIN_SIZE = 32
private const val PASSWORD_SIZE = 32


// ==========================================
// РЕЗУЛЬТАТ АВТОРИЗАЦИИ
// ==========================================

sealed class AuthResult {

    // ======================================
    // УСПЕШНАЯ АВТОРИЗАЦИЯ
    // ======================================

    data object Success : AuthResult()


    // ======================================
    // НЕВЕРНЫЙ ЛОГИН ИЛИ ПАРОЛЬ
    // ======================================

    data object WrongLoginPassword : AuthResult()


    // ======================================
    // ВЕРСИЯ ПРИЛОЖЕНИЯ УСТАРЕЛА
    // ======================================

    data object AppOutdated : AuthResult()
}


// ==========================================
// СОЗДАНИЕ БИНАРНОГО ПАКЕТА
// ==========================================

private fun createAuthPacket(
    key: String,
    login: String,
    password: String
): ByteArray {

    val keyBytes = key.toByteArray(Charsets.UTF_8)
    val loginBytes = login.toByteArray(Charsets.UTF_8)
    val passwordBytes = password.toByteArray(Charsets.UTF_8)


    // ======================================
    // ПРОВЕРЯЕМ РАЗМЕРЫ
    // ======================================

    require(keyBytes.size <= KEY_SIZE) {
        "Key слишком длинный"
    }

    require(loginBytes.size <= LOGIN_SIZE) {
        "Login слишком длинный"
    }

    require(passwordBytes.size <= PASSWORD_SIZE) {
        "Password слишком длинный"
    }


    // ======================================
    // СОЗДАЁМ ПАКЕТ
    // ======================================

    val packet = ByteArray(
        KEY_SIZE +
                LOGIN_SIZE +
                PASSWORD_SIZE
    )


    // ======================================
    // ЗАПИСЫВАЕМ KEY
    // ======================================

    System.arraycopy(
        keyBytes,
        0,
        packet,
        0,
        keyBytes.size
    )


    // ======================================
    // ЗАПИСЫВАЕМ LOGIN
    // ======================================

    System.arraycopy(
        loginBytes,
        0,
        packet,
        KEY_SIZE,
        loginBytes.size
    )


    // ======================================
    // ЗАПИСЫВАЕМ PASSWORD
    // ======================================

    System.arraycopy(
        passwordBytes,
        0,
        packet,
        KEY_SIZE + LOGIN_SIZE,
        passwordBytes.size
    )


    return packet
}


// ==========================================
// АВТОРИЗАЦИЯ
// ==========================================

suspend fun authorize(
    login: String,
    password: String,
    onResult: (AuthResult) -> Unit
) = withContext(Dispatchers.IO) {

    val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()


    // ======================================
    // СОЗДАЁМ WEBSOCKET REQUEST
    // ======================================

    val request = Request.Builder()
        .url(SERVER_URL)
        .build()


    // ======================================
    // СОЗДАЁМ WEBSOCKET
    // ======================================

    client.newWebSocket(
        request,
        object : WebSocketListener() {

            // ==================================
            // СОЕДИНЕНИЕ УСТАНОВЛЕНО
            // ==================================

            override fun onOpen(
                webSocket: WebSocket,
                response: okhttp3.Response
            ) {

                println(
                    "WebSocket соединение установлено"
                )


                // ==============================
                // СОЗДАЁМ AUTH PACKET
                // ==============================

                val packet = createAuthPacket(
                    SERVER_KEY,
                    login,
                    password
                )


                // ==============================
                // ОТПРАВЛЯЕМ БИНАРНЫЙ ПАКЕТ
                // ==============================

                webSocket.send(
                    ByteString.of(*packet)
                )
            }


            // ==================================
            // ПОЛУЧИЛИ BINARY ОТВЕТ
            // ==================================

            override fun onMessage(
                webSocket: WebSocket,
                bytes: ByteString
            ) {

                val response = bytes.toByteArray()


                // ==================================
                // ПУСТОЙ ОТВЕТ
                // ==================================

                if (response.isEmpty()) {

                    println(
                        "Получен пустой ответ от сервера"
                    )

                    onResult(
                        AuthResult.AppOutdated
                    )

                    webSocket.close(
                        1000,
                        "Application outdated"
                    )

                    return
                }


                // ==================================
                // AUTH_OK
                // ==================================

                if (
                    response.contentEquals(
                        "AUTH_OK".toByteArray()
                    )
                ) {

                    println(
                        "AUTH_OK получен"
                    )


                    // ==============================
                    // СОХРАНЯЕМ СОЕДИНЕНИЕ
                    // ==============================

                    ServerConnection.setConnection(
                        webSocket
                    )


                    // ==============================
                    // ВАЖНО:
                    //
                    // ЗДЕСЬ ЕЩЁ НЕ ВЫЗЫВАЕМ
                    // AuthResult.Success
                    //
                    // СНАЧАЛА СЕРВЕР ПРИШЛЁТ
                    // AUTH_USER
                    // ==============================

                    println(
                        "Ожидаем данные текущего пользователя..."
                    )


                    return
                }


                // ==================================
                // AUTH_USER
                //
                // ПАКЕТ:
                //
                // AUTH_USER
                // + unic
                // + ava
                // + nik
                // ==================================

                if (
                    response.size >= 9 &&
                    String(
                        response.copyOfRange(
                            0,
                            9
                        ),
                        Charsets.UTF_8
                    ) == "AUTH_USER"
                ) {

                    println(
                        "Получен пакет AUTH_USER"
                    )


                    // ==============================
                    // ПЕРЕДАЁМ ПАКЕТ
                    // В SERVER CONNECTION
                    // ==============================

                    ServerConnection.receiveMyUser(
                        response
                    )


                    // ==============================
                    // ПРОВЕРЯЕМ ЧТО ДАННЫЕ
                    // ДЕЙСТВИТЕЛЬНО ПОЛУЧЕНЫ
                    // ==============================

                    if (
                        ServerConnection.myUnic != null &&
                        ServerConnection.myAva != null &&
                        ServerConnection.myNik != null
                    ) {

                        println(
                            "Данные текущего пользователя получены"
                        )

                        println(
                            "unic = ${ServerConnection.myUnic}"
                        )

                        println(
                            "ava = ${ServerConnection.myAva}"
                        )

                        println(
                            "nik = ${ServerConnection.myNik}"
                        )


                        // ==============================
                        // ТЕПЕРЬ АВТОРИЗАЦИЯ ПОЛНОСТЬЮ
                        // ЗАВЕРШЕНА
                        // ==============================

                        onResult(
                            AuthResult.Success
                        )

                    } else {

                        println(
                            "Ошибка: данные текущего пользователя не получены"
                        )


                        onResult(
                            AuthResult.AppOutdated
                        )


                        webSocket.close(
                            1000,
                            "User data error"
                        )
                    }


                    return
                }


                // ==================================
                // NOLOG
                // ==================================

                if (
                    response.contentEquals(
                        "NOLOG".toByteArray()
                    )
                ) {

                    println(
                        "Неверный логин или пароль"
                    )


                    onResult(
                        AuthResult.WrongLoginPassword
                    )


                    webSocket.close(
                        1000,
                        "Authorization failed"
                    )

                    return
                }


                // ==================================
                // UNVER
                // ==================================

                if (
                    response.contentEquals(
                        "UNVER".toByteArray()
                    )
                ) {

                    println(
                        "Версия приложения устарела"
                    )


                    onResult(
                        AuthResult.AppOutdated
                    )


                    webSocket.close(
                        1000,
                        "Application outdated"
                    )

                    return
                }


                // ==================================
                // ДОПОЛНИТЕЛЬНЫЕ BINARY ПАКЕТЫ
                // ==================================

                println(
                    "Получен дополнительный BINARY пакет"
                )

                println(
                    "Размер: ${response.size} байт"
                )


                // ==================================
                // ЧИТАЕМ ПЕРВЫЙ БАЙТ
                // ==================================

                val packetType =
                    response[0].toInt().toChar()


                println(
                    "Тип пакета: $packetType"
                )


                // ==================================
                // ПАКЕТ ЧАТА
                //
                // c + chat_id + unic_id +
                // nickname + ava
                // ==================================

                if (packetType == 'c') {

                    println(
                        "Это пакет чата"
                    )


                    ServerConnection.receiveBinary(
                        response
                    )


                    return
                }


                // ==================================
                // ПАКЕТ СООБЩЕНИЯ
                //
                // C + данные сообщения
                // ==================================

                if (packetType == 'C') {

                    println(
                        "Это пакет сообщения"
                    )


                    ServerConnection.receiveMessage(
                        response
                    )


                    return
                }


                // ==================================
                // НЕИЗВЕСТНЫЙ ПАКЕТiles
                // ==================================

                println(
                    "Неизвестный тип BINARY пакета: $packetType"
                )
            }


            // ==================================
            // ОШИБКА / НЕТ ОТВЕТА
            // ==================================

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {

                println(
                    "Нет корректного ответа от сервера: ${t.message}"
                )


                onResult(
                    AuthResult.AppOutdated
                )
            }
        }
    )
}