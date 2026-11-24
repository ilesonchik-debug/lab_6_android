import asyncio
import websockets
import psycopg2
import sys



# ==========================================
# НАСТРОЙКИ WEBSOCKET
# ==========================================

HOST = "0.0.0.0"
PORT = 8222


# ==========================================
# НАСТРОЙКИ DATABASE
# ==========================================

DB_HOST = "127.0.0.1"
DB_PORT = 8888

DB_NAME = "d1alog"
DB_USER = "postgres"
DB_PASSWORD = "qwert12345"


# ==========================================
# ГЛОБАЛЬНОЕ СОЕДИНЕНИЕ С DATABASE
# ==========================================

db = None


# ==========================================
# ГЛОБАЛЬНЫЕ ДАННЫЕ
# ==========================================

users = []
chats = []
online = []
messages = []

msg_count = 0

sys.modules["dialog_hub"] = sys.modules[__name__]

# ==========================================
# ПОДКЛЮЧЕНИЕ К DATABASE
# ==========================================

def connect_database():

    global db

    print(
        "Подключение к базе данных..."
    )

    try:

        db = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )

        print(
            "Соединение с PostgreSQL установлено"
        )

        return True

    except Exception as error:

        print(
            "Ошибка подключения к базе данных:"
        )

        print(
            error
        )

        db = None

        return False


# ==========================================
# ОБРАБОТЧИК WEBSOCKET
# ==========================================

async def websocket_handler(websocket):

    print(
        "Новое WebSocket подключение"
    )


    # ======================================
    # АВТОРИЗАЦИЯ
    # ======================================

    import dialog_auth

    user = await dialog_auth.authenticate_client(
        websocket
    )


    # ======================================
    # АВТОРИЗАЦИЯ НЕ ПРОЙДЕНА
    # ======================================

    if user is None:

        print(
            "Клиент не авторизован"
        )

        return


    # ======================================
    # АВТОРИЗАЦИЯ УСПЕШНА
    # ======================================

    print(
        "Пользователь авторизован:",
        user["nik"]
    )


    # ======================================
    # ПРИЁМ ПАКЕТОВ ОТ КЛИЕНТА
    # ======================================

    import dialog_a


    try:

        async for packet in websocket:

            await dialog_a.receive_packet(
                websocket,
                packet
            )


    except websockets.ConnectionClosed:

        pass


    finally:

        online[:] = [
            online_user
            for online_user in online
            if online_user["websocket"] is not websocket
        ]


# ==========================================
# ЗАПУСК СЕРВЕРА
# ==========================================

async def main():

    global db

    # ======================================
    # ПОДКЛЮЧАЕМСЯ К DATABASE
    # ======================================

    if not connect_database():

        print(
            "Сервер остановлен."
        )

        return

    import dialog_auth
    dialog_auth.load_users() # данные для другго файла

    import chat
    chat.load_chats()

    import dialog_msg
    dialog_msg.load_messages()


    # ======================================
    # СЕРВЕР ЗАПУЩЕН
    # ======================================

    print(
        "WebSocket сервер запускается..."
    )

    print(
        "Адрес:",
        HOST,
        PORT
    )

    try:

        async with websockets.serve(
            websocket_handler,
            HOST,
            PORT,
            ping_interval=None
        ):

            print(
                "WebSocket сервер запущен"
            )

            await asyncio.Future()

    finally:

        # ==================================
        # ЗАКРЫВАЕМ DATABASE
        # ==================================

        if db is not None:

            db.close()

            print(
                "Соединение с PostgreSQL закрыто"
            )


# ==========================================
# START
# ==========================================

if __name__ == "__main__":

    asyncio.run(
        main()
    )
