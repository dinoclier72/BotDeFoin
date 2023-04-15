package fr.ensim.interop.introrest.controller;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramController extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "MegabotdefointavuBot";
    }

    @Override
    public String getBotToken() {
        return "6274102394:AAGBqaOoBqHFaFKAvR6spJzH9JvYnaxYhkg";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User user = message.getFrom();

        Pattern pattern = Pattern.compile("quoi", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message.getText());
        boolean matchFound = matcher.find();
        if(matchFound) {
            sendText(user.getId(),"Feur");
        } else {
            sendText(user.getId(),"T'es cringe frerot");
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }
}
