package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.model.joke.Joke;
import fr.ensim.interop.introrest.model.meteo.List;
import fr.ensim.interop.introrest.model.meteo.OpenWeather;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        IDLE,
        WAITING_FOR_JOKE_ID
    }
    private HashMap<Long,BotState> userBotState = new HashMap<Long,BotState>();
    private void resetUserState(long user){
        setUserBotState(user,BotState.IDLE);
    }
    private void setUserBotState(long user, BotState botState){
        userBotState.put(user,botState);
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
            resetUserState(userID);
        //on regarde selon l'état du bot
        switch (userBotState.get(userID)){
            case IDLE:
                switch (messageText){
                    case "/meteo":
                        setUserBotState(userID,BotState.WAITING_FOR_CITY);
                        sendText(userID,"Mode meteo activé, donnez maintenant une ville pour la recherche");
                        break;
                    default:
                        Pattern pattern = Pattern.compile("quoi", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(message.getText());
                        boolean matchFound = matcher.find();
                        if(matchFound) {
                            sendText(userID,"Feur");
                        }
                        break;
                    case "/blague":
                        Joke joke = restTemplate.getForObject(localURL+"/blague",Joke.class);
                        sendText(userID,formatBlague(joke));
                        break;
                    case "/blaguespecifique":
                        setUserBotState(userID,BotState.WAITING_FOR_JOKE_ID);
                        sendText(userID,"Mode blague specifique activé, donnez maintenant l'id de la blague recherchée");
                        break;
                }
                break;
            case WAITING_FOR_CITY:
                String bulletin = getMeteoBulletin(messageText);
                sendText(userID,bulletin);
                resetUserState(userID);
                break;
            case WAITING_FOR_JOKE_ID:
                int id = Integer.parseInt(messageText);
                sendText(userID,grabJoke(id));
                resetUserState(userID);
                break;
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
    //obtenir un bulletin meteo
    public String getMeteoBulletin(String cityName){
        String url = localURL+"/meteo?cityName="+cityName;
        try{
            ResponseEntity<OpenWeather> responseEntity = restTemplate.getForEntity(url,OpenWeather.class);
            OpenWeather openWeather = responseEntity.getBody();
            List list = openWeather.list.get(0);
            cityName = openWeather.city.name;
            Double temp = list.main.temp;
            String weather = list.weather.get(0).description;
            double windSpeed = list.wind.speed;
            int windDirection = list.wind.deg;
            String bulletin = String.format("Bulletin météo pour %s : \nTempérature : %s°C\nMétéo : %s\nVitesse du vent : %s m/s\nDirection du vent : %s degrés", cityName, temp, weather, windSpeed, windDirection);
            return bulletin;
        }catch (HttpClientErrorException e){
            //ville non trouvée
            return "Ville demandée incorect";
        }
    }
    //formater une blague
    public String formatBlague(Joke joke){
        String exit = "";
        exit += joke.getTitle();
        exit +="\n\n";
        exit += joke.getContent();
        exit += "\n\n";
        exit += "catégorie : " + joke.getCategory() + "   ";
        exit += "note : " +joke.getGrade()+ "/10";
        return  exit;
    }
    //obtenir une blague
    public String grabJoke(int id){
        try {
            ResponseEntity<Joke> responseEntity = restTemplate.getForEntity(localURL+"/blague?id="+id,Joke.class);
            Joke joke = responseEntity.getBody();
            return formatBlague(joke);
        }catch (HttpClientErrorException errorException){
            return "Ta blague n'existe pas";
        }
    }
}
