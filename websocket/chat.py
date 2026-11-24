import struct

import dialog_hub


# ==========================================
# ЗАГРУЗКА ЧАТОВ
# ==========================================

def load_chats():

    print(
        "Загрузка чатов..."
    )


    try:

        cursor = dialog_hub.db.cursor()


        # ==================================
        # ПОЛУЧАЕМ ЧАТЫ ИЗ DATABASE
        # ==================================

        cursor.execute(
            """
            SELECT
                chat_id,
                unic_1,
                unic_2
            FROM chats
            """
        )


        rows = cursor.fetchall()


        cursor.close()


        # ==================================
        # СОХРАНЯЕМ В GLOBAL CHATS
        # ИЗ DIALOG_HUB
        # ==================================

        dialog_hub.chats.clear()


        for row in rows:

            dialog_hub.chats.append(
                {
                    "chat_id": row[0],
                    "unic_1": row[1],
                    "unic_2": row[2]
                }
            )


        print(
            "Чаты загружены:",
            len(dialog_hub.chats)
        )


        return True


    except Exception as error:

        print(
            "Ошибка загрузки чатов:"
        )

        print(
            error
        )


        return False


# ==========================================
# ПОИСК ПОЛЬЗОВАТЕЛЯ
# ==========================================

def find_user(unic_id):

    for user in dialog_hub.users:

        if user["unic_id"] == unic_id:

            return user


    return None


# ==========================================
# ПОЛУЧЕНИЕ СОБЕСЕДНИКА
# ==========================================

def get_partner(
    chat,
    user_unic
):

    if chat["unic_1"] == user_unic:

        return find_user(
            chat["unic_2"]
        )


    if chat["unic_2"] == user_unic:

        return find_user(
            chat["unic_1"]
        )


    return None


# ==========================================
# ПОЛУЧЕНИЕ ЧАТОВ ПОЛЬЗОВАТЕЛЯ
# ==========================================

def get_user_chats(user_unic):

    result = []

    print(
        "Поиск чатов для unic:",
        user_unic
    )

    print(
        "Всего чатов в памяти:",
        len(dialog_hub.chats)
    )

    for current_chat in dialog_hub.chats:

        '''
        print(
            "Проверяем чат:",
            current_chat
        )
        '''

        if (
            current_chat["unic_1"] == user_unic
            or
            current_chat["unic_2"] == user_unic
        ):

            print(
                "Чат подходит:",
                current_chat["chat_id"]
            )

            partner = get_partner(
                current_chat,
                user_unic
            )

            if partner is None:

                print(
                    "Собеседник не найден:",
                    current_chat
                )

                continue

            result.append(
                {
                    "chat_id": current_chat["chat_id"],
                    "unic_id": partner["unic_id"],
                    "nickname": partner["nik"],
                    "ava": partner["ava"]
                }
            )

    '''
    print(
        "Найдено чатов:",
        len(result)
    )
    '''

    return result


# ==========================================
# СОЗДАНИЕ БИНАРНОГО ПАКЕТА ОДНОГО ЧАТА
# ==========================================

def create_chat_packet(
    current_chat
):

    chat_id = str(
        current_chat["chat_id"]
    ).encode("utf-8")


    unic_id = str(
        current_chat["unic_id"]
    ).encode("utf-8")


    nickname = str(
        current_chat["nickname"]
    ).encode("utf-8")


    ava = str(
        current_chat["ava"]
    ).encode("utf-8")


    # ======================================
    # ПРОВЕРЯЕМ РАЗМЕРЫ
    # ======================================

    if len(chat_id) > 255:

        raise ValueError(
            "chat_id слишком длинный"
        )


    if len(unic_id) > 255:

        raise ValueError(
            "unic_id слишком длинный"
        )


    if len(nickname) > 255:

        raise ValueError(
            "nickname слишком длинный"
        )


    if len(ava) > 255:

        raise ValueError(
            "ava слишком длинный"
        )


    # ======================================
    # КЛЮЧ ПАКЕТА
    # ======================================

    packet = b"c"


    # ======================================
    # CHAT_ID
    # ======================================

    packet += struct.pack(
        "!B",
        len(chat_id)
    )

    packet += chat_id


    # ======================================
    # UNIC_ID
    # ======================================

    packet += struct.pack(
        "!B",
        len(unic_id)
    )

    packet += unic_id


    # ======================================
    # NICKNAME
    # ======================================

    packet += struct.pack(
        "!B",
        len(nickname)
    )

    packet += nickname


    # ======================================
    # AVA
    # ======================================

    packet += struct.pack(
        "!B",
        len(ava)
    )

    packet += ava


    return packet


# ==========================================
# ОТПРАВКА ЧАТОВ ПОЛЬЗОВАТЕЛЮ
# ==========================================

async def send_user_chats(
    websocket,
    user
):

    user_unic = user["unic_id"]


    # ======================================
    # ПОЛУЧАЕМ ЧАТЫ ПОЛЬЗОВАТЕЛЯ
    # ======================================

    user_chats = get_user_chats(
        user_unic
    )


    # ======================================
    # ОТПРАВЛЯЕМ КАЖДЫЙ ЧАТ ОТДЕЛЬНО
    # ======================================

    for current_chat in user_chats:

        packet = create_chat_packet(
            current_chat
        )


        # ==================================
        # ОТПРАВЛЯЕМ BINARY
        # ==================================

        await websocket.send(
            packet
        )


        print(
            "Чат отправлен:",
            user["nik"],
            "|",
            current_chat["chat_id"]
        )


    # ======================================
    # ИТОГ
    # ======================================

    print(
        "Всего чатов отправлено:",
        user["nik"],
        "|",
        len(user_chats)
    )
