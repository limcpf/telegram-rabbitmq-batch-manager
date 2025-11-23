package com.example.batch.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.serialization.MaybeInaccessibleMessageDeserializer;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@RegisterForReflection(targets = {
        // Core
        ApiResponse.class,
        Update.class,
        Message.class,
        User.class,
        Chat.class,
        DefaultBotSession.class,
        MaybeInaccessibleMessageDeserializer.class,

        // Common Objects
        MessageEntity.class,
        PhotoSize.class,
        Audio.class,
        Document.class,
        Video.class,
        Voice.class,
        Contact.class,
        Location.class,
        Venue.class,
        Poll.class,
        PollOption.class,
        ChatMember.class,
        CallbackQuery.class,

        // Keyboards
        ReplyKeyboardMarkup.class,
        InlineKeyboardMarkup.class,
        KeyboardRow.class,
        KeyboardButton.class,
        InlineKeyboardButton.class,

        // Methods
        SendMessage.class
}, serialization = true, ignoreNested = false)
public class TelegramReflectionConfig {
}
