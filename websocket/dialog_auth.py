import asyncio
import struct
import websockets

import dialog_hub
import chat
import dialog_msg


# ==========================================
# НАСТРОЙКИ АВТОРИЗАЦИИ
# ==========================================

SERVER_KEY = b"ebin410"


# ==========================================
# РАЗМЕРЫ БИНАРНОГО ПАКЕТА
# ==========================================

KEY_SIZE = 7
LOGIN_SIZE = 32
PASSWORD_SIZE = 32


# ==========================================
# ЗАГРУЗКА USERS ИЗ DATABASE
# ==========================================
# Пользователи загружаются один раз
# при запуске сервера.
#
# Список хранится в:
#
# dialog_hub.users
#
# ==========================================

def load_users():

    # ======================================
    # ЕСЛИ USERS УЖЕ ЗАГРУЖЕНЫ
    # ======================================

    if dialog_hub.users:

        return


    print(
        "Загрузка пользователей..."
    )


    try:

        cursor = dialog_hub.db.cursor()


        # ==================================
        # ПОЛУЧАЕМ ДАННЫЕ ИЗ ТАБЛИЦЫ unic
        # ==================================

        cursor.execute(
            """
            SELECT
                log,
                pas,
                nik,
                ava,
                unic
            FROM unic
            """
        )


        rows = cursor.fetchall()


        cursor.close()


        # ==================================
        # ЗАПОЛНЯЕМ GLOBAL USERS
        # В dialog_hub.py
        # ==================================

        dialog_hub.users.clear()


        for row in rows:

            dialog_hub.users.append(
                {
                    "log": row[0],
                    "pas": row[1],
                    "nik": row[2],
                    "ava": row[3],
                    "unic_id": row[4]
                }
            )


        print(
            "Пользователи загружены:",
            len(dialog_hub.users)
        )


    except Exception as error:

        print(
            "Ошибка загрузки пользователей:"
        )

        print(
            error
        )


# ==========================================
# СОЗДАНИЕ ПАКЕТА АВТОРИЗАЦИИ
# ==========================================

def create_auth_packet(
    key,
    login,
    password
):

    key = key.encode(
        "utf-8"
    )

    login = login.encode(
        "utf-8"
    )

    password = password.encode(
        "utf-8"
    )


    # ======================================
    # ПРОВЕРЯЕМ РАЗМЕРЫ
    # ======================================

    if len(key) > KEY_SIZE:

        raise ValueError(
            "Key слишком длинный"
        )


    if len(login) > LOGIN_SIZE:

        raise ValueError(
            "Login слишком длинный"
        )


    if len(password) > PASSWORD_SIZE:

        raise ValueError(
            "Password слишком длинный"
        )


    # ======================================
    # ДОПОЛНЯЕМ НУЛЯМИ
    # ======================================

    key = key.ljust(
        KEY_SIZE,
        b"\0"
    )

    login = login.ljust(
        LOGIN_SIZE,
        b"\0"
    )

    password = password.ljust(
        PASSWORD_SIZE,
        b"\0"
    )


    # ======================================
    # СОБИРАЕМ ПАКЕТ
    # ======================================

    return struct.pack(
        f"!{KEY_SIZE}s{LOGIN_SIZE}s{PASSWORD_SIZE}s",
        key,
        login,
        password
    )


# ==========================================
# РАСПАКОВКА ПАКЕТА АВТОРИЗАЦИИ
# ==========================================

def read_auth_packet(packet):

    packet_size = (
        KEY_SIZE +
        LOGIN_SIZE +
        PASSWORD_SIZE
    )


    # ======================================
    # ПРОВЕРЯЕМ РАЗМЕР
    # ======================================

    if len(packet) != packet_size:

        return None


    # ======================================
    # РАСПАКОВЫВАЕМ
    # ======================================

    key, login, password = struct.unpack(
        f"!{KEY_SIZE}s{LOGIN_SIZE}s{PASSWORD_SIZE}s",
        packet
    )


    # ======================================
    # УБИРАЕМ НУЛИ
    # ======================================

    key = key.rstrip(
        b"\0"
    ).decode(
        "utf-8"
    )


    login = login.rstrip(
        b"\0"
    ).decode(
        "utf-8"
    )


    password = password.rstrip(
        b"\0"
    ).decode(
        "utf-8"
    )


    return (
        key,
        login,
        password
    )


# ==========================================
# СОЗДАНИЕ ПАКЕТА ДАННЫХ АВТОРИЗОВАННОГО
# ПОЛЬЗОВАТЕЛЯ
# ==========================================
#
# Пакет содержит:
#
# AUTH_USER
# unic
# ava
# nik
#
# Каждая строка передаётся как:
#
# 2 байта длины
# + данные UTF-8
#
# ==========================================

def create_user_info_packet(
    user
):

    unic = str(
        user["unic_id"]
    ).encode(
        "utf-8"
    )

    ava = str(
        user["ava"]
    ).encode(
        "utf-8"
    )

    nik = str(
        user["nik"]
    ).encode(
        "utf-8"
    )


    # ======================================
    # ПРОВЕРЯЕМ РАЗМЕРЫ
    # ======================================

    if len(unic) > 65535:

        raise ValueError(
            "unic слишком длинный"
        )


    if len(ava) > 65535:

        raise ValueError(
            "ava слишком длинный"
        )


    if len(nik) > 65535:

        raise ValueError(
            "nik слишком длинный"
        )


    # ======================================
    # СОБИРАЕМ ПАКЕТ
    # ======================================

    packet = (
        b"AUTH_USER"
        +
        struct.pack(
            "!H",
            len(unic)
        )
        +
        unic
        +
        struct.pack(
            "!H",
            len(ava)
        )
        +
        ava
        +
        struct.pack(
            "!H",
            len(nik)
        )
        +
        nik
    )


    return packet


# ==========================================
# ПОИСК ПОЛЬЗОВАТЕЛЯ
# ==========================================

def find_user(
    login,
    password
):

    # ======================================
    # ИЩЕМ В GLOBAL USERS
    # ИЗ dialog_hub.py
    # ======================================

    for user in dialog_hub.users:

        if (
            user["log"] == login
            and
            user["pas"] == password
        ):

            return user


    return None


# ==========================================
# ОБРАБОТКА АВТОРИЗАЦИИ
# ==========================================

async def authenticate_client(
    websocket
):

    try:

        # ==================================
        # ЖДЁМ ПАКЕТ АВТОРИЗАЦИИ
        # ==================================

        packet = await asyncio.wait_for(
            websocket.recv(),
            timeout=10
        )


        # ==================================
        # ПРОВЕРЯЕМ ЧТО ПАКЕТ БИНАРНЫЙ
        # ==================================

        if not isinstance(
            packet,
            bytes
        ):

            await websocket.send(
                b"BAD_PACKET"
            )

            await websocket.close()

            return None


        # ==================================
        # РАСПАКОВЫВАЕМ ПАКЕТ
        # ==================================

        auth = read_auth_packet(
            packet
        )


        if auth is None:

            await websocket.send(
                b"BAD_PACKET"
            )

            await websocket.close()

            return None


        key, login, password = auth


        print(
            "Попытка входа:",
            login
        )


        # ==================================
        # ПРОВЕРЯЕМ SERVER KEY
        # ==================================

        if key != SERVER_KEY.decode(
            "utf-8"
        ):

            print(
                "Неверный ключ сервера"
            )


            await websocket.send(
                b"UNVER"
            )


            await websocket.close()

            return None


        # ==================================
        # ПРОВЕРЯЕМ ЛОГИН И ПАРОЛЬ
        # ==================================

        user = find_user(
            login,
            password
        )


        # ==================================
        # НЕВЕРНЫЙ ЛОГИН ИЛИ ПАРОЛЬ
        # ==================================

        if user is None:

            print(
                "Неверный логин или пароль:",
                login
            )


            await websocket.send(
                b"NOLOG"
            )


            await websocket.close()

            return None


        # ==================================
        # АВТОРИЗАЦИЯ УСПЕШНА
        # ==================================

        print(
            "Пользователь авторизован:",
            user["nik"]
        )

        dialog_hub.online.append(
            {
                "websocket": websocket,
                "unic_id": user["unic_id"]
            }
        )


        # ==================================
        # ОТПРАВЛЯЕМ AUTH_OK
        # ==================================

        await websocket.send(
            b"AUTH_OK"
        )


        # ==================================
        # ОТПРАВЛЯЕМ ДАННЫЕ ТЕКУЩЕГО
        # АВТОРИЗОВАННОГО ПОЛЬЗОВАТЕЛЯ
        # ==================================

        user_info_packet = create_user_info_packet(
            user
        )


        await websocket.send(
            user_info_packet
        )


        # ==================================
        # ОТПРАВЛЯЕМ ЧАТЫ ПОЛЬЗОВАТЕЛЯ
        # ==================================

        await chat.send_user_chats(
            websocket,
            user
        )


        await dialog_msg.send_user_messages(
            websocket,
            user
        )


        # ==================================
        # ВОЗВРАЩАЕМ ПОЛЬЗОВАТЕЛЯ
        # ==================================

        return user


    # ======================================
    # ТАЙМАУТ АВТОРИЗАЦИИ
    # ======================================

    except asyncio.TimeoutError:

        print(
            "Клиент не отправил авторизацию"
        )


        await websocket.close()

        return None


    # ======================================
    # СОЕДИНЕНИЕ ЗАКРЫТО
    # ======================================

    except websockets.ConnectionClosed:

        return None


    # ======================================
    # ПРОЧАЯ ОШИБКА
    # ======================================

    except Exception as error:

        print(
            "Ошибка авторизации:"
        )

        print(
            error
        )


        try:

            await websocket.close()

        except Exception:

            pass


        return None


    
