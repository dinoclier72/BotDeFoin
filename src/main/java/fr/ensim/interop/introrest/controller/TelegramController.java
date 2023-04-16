package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.ChatBotApplication;
import fr.ensim.interop.introrest.model.meteo.List;
import fr.ensim.interop.introrest.model.meteo.OpenWeather;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;

public class TelegramController extends TelegramLongPollingBot {
    /*
    	//section URL de l'api
	@Value("${server.port}")
	private static int serverPort;
	private static String localURL;
	//necessaire pour un fonctionnement dynamique du port de l'API
	public static void init() {
		localURL = "http://127.0.0.1:" + serverPort;
	}
     */
    private String localURL = "http://127.0.0.1:9090";
    //etat de la conversation avec le bot
    private enum BotState{
        WAITING_FOR_CITY,
        IDLE
    }
    private HashMap<Long,BotState> userBotState = new HashMap<Long,BotState>();
    private void resetUserState(long user){
        userBotState.put(user,BotState.IDLE);
    }
    private RestTemplate restTemplate = new RestTemplate();
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
        //pas de traitement si il n'y as pas de message
        if(!update.hasMessage())
            return;
        //fonction de base de telegram
        Message message = update.getMessage();
        User user = message.getFrom();
        Long userID = user.getId();
        String messageText = message.getText();
        //on rajoute l'utilisateur actuelle à la map si besoin
        if(!userBotState.containsKey(userID))
            userBotState.put(userID,BotState.IDLE);
        //on regarde selon l'état du bot
        switch (userBotState.get(userID)){
            case IDLE:
                switch (messageText){
                    case "/meteo":
                        userBotState.put(userID,BotState.WAITING_FOR_CITY);
                        sendText(userID,"Mode meteo activé, donnez maintenant une ville pour la recherche");
                        break;
                    case "/blague":
                        break;
                }
                break;
            case WAITING_FOR_CITY:
                String bulletin = getMeteoBulletin(messageText);
                sendText(userID,bulletin);
                resetUserState(userID);
                break;
        }

        /*
                        OpenWeather openWeather = restTemplate.getForObject("http://127.0.0.1:9090/meteo")
                String bulletinMeteo = "--------------------------------------------------------\n" +
                        "               Bulletin Météo pour " + jsonObject.getJSONObject("city").getString("name") + "\n" +
                        "--------------------------------------------------------\n" +
                        "Date et Heure : " + dateAndTime + "\n" +
                        "Conditions météo actuelles : " + weatherDescription + "\n\n" +
                        "Température : " + temperature + "°C\n" +
                        "Température minimale : " + tempMin + "°C\n" +
                        "Température maximale : " + tempMax + "°C\n" +
                        "Humidité : " + humidity + "%\n" +
                        "Visibilité : " + visibility + " mètres\n" +
                        "Vitesse du vent : " + windSpeed + " m/s\n" +
                        "Direction du vent : " + windDirection + " degrés\n" +
                        "Rafales de vent : " + windGust + " m/s\n\n" +
                        "Merci d'avoir consulté notre bulletin météo !";
        Pattern pattern = Pattern.compile("quoi", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message.getText());
        boolean matchFound = matcher.find();
        if(matchFound) {
            sendText(user.getId(),"Feur");
        } else {
            sendText(user.getId(),"T'es cringe frerot");
        }*/
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

    public String getMeteoBulletin(String cityName){
        String url = localURL+"/meteo?cityName="+cityName;
        System.out.println(url);
        ResponseEntity<OpenWeather> responseEntity = restTemplate.getForEntity(url,OpenWeather.class);
        if(responseEntity.getStatusCode().is4xxClientError()){
            return "Ville demandée incorect";
        }
        OpenWeather openWeather = responseEntity.getBody();
        List list = openWeather.list.get(0);
        cityName = openWeather.city.name;
        Double temp = list.main.temp;
        String weather = list.weather.get(0).description;
        double windSpeed = list.wind.speed;
        int windDirection = list.wind.deg;
        String bulletin = String.format("Bulletin météo pour %s : \nTempérature : %s°C\nMétéo : %s\nVitesse du vent : %s m/s\nDirection du vent : %s degrés", cityName, temp, weather, windSpeed, windDirection);
        return bulletin;
    }
}
