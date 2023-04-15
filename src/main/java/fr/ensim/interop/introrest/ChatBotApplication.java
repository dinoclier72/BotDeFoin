package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.controller.TelegramController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class ChatBotApplication {

	public static void main(String[] args) throws TelegramApiException {
		//SpringApplication.run(ChatBotApplication.class, args);
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(new TelegramController());
	}
	
	
}
