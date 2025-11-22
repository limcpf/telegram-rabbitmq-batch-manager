package com.example.batch.bot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TelegramBatchBot extends TelegramLongPollingBot {

    @ConfigProperty(name = "telegram-bot-token")
    String botToken;

    @Inject
    com.example.batch.rabbitmq.BatchProducer producer;

    @Inject
    com.example.batch.scheduler.BatchScheduler batchScheduler;

    @Override
    public String getBotUsername() {
        return "QuarkusBatchBot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String[] parts = messageText.split("\\s+");
            String command = parts[0];

            String response;
            try {
                response = switch (command) {
                    case "/new" -> handleNew(parts);
                    case "/remove" -> handleRemove(parts);
                    case "/list" -> handleList(parts);
                    case "/exec" -> handleExec(parts);
                    case "/send" -> handleSend(parts);
                    default -> "알 수 없는 명령어입니다: " + command;
                };
            } catch (Exception e) {
                response = "오류 발생: " + e.getMessage();
            }

            sendMessage(chatId, response);
        }
    }

    private String handleNew(String[] parts) {
        if (parts.length < 4) {
            return "사용법: /new {이름} {크론} {메시지}";
        }
        String name = parts[1];
        String cron = parts[2];
        String message = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
        batchScheduler.schedule(name, cron, message);
        return "배치 등록됨: " + name;
    }

    private String handleRemove(String[] parts) {
        if (parts.length < 2) {
            return "사용법: /remove {이름}";
        }
        String name = parts[1];
        batchScheduler.remove(name);
        return "배치 삭제됨: " + name;
    }

    private String handleList(String[] parts) {
        int limit = 10;
        if (parts.length > 1) {
            try {
                limit = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return "유효하지 않은 숫자입니다.";
            }
        }
        return batchScheduler.list(limit);
    }

    private String handleExec(String[] parts) {
        if (parts.length < 2) {
            return "사용법: /exec {이름}";
        }
        String name = parts[1];
        batchScheduler.execute(name);
        return "배치 실행됨: " + name;
    }

    private String handleSend(String[] parts) {
        if (parts.length < 2) {
            return "사용법: /send {메시지}";
        }
        String message = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        producer.send(message);
        return "메시지 전송됨: " + message;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
