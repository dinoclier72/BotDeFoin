package fr.ensim.interop.introrest;

import fr.ensim.interop.introrest.chains.JokeChain;
import fr.ensim.interop.introrest.chains.MeteoChain;
import fr.ensim.interop.introrest.model.joke.Joke;
import fr.ensim.interop.introrest.model.meteo.List;
import fr.ensim.interop.introrest.model.meteo.OpenWeather;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;
import java.util.Random;
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
        WAITING_FOR_FORECAST_DELAY,
        IDLE,
        WAITING_FOR_JOKE_ID,
        WAITING_FOR_JOKE_TITLE,
        WAITING_FOR_JOKE_CONTENT,
        WAITING_FOR_JOKE_CATEGORY,
        WAITING_FOR_JOKE_GRADE
    }
    private HashMap<Long,BotState> userBotState = new HashMap<Long,BotState>();
    private void resetUserState(long user){
        setUserBotState(user,BotState.IDLE);
    }
    private void setUserBotState(long user, BotState botState){
        userBotState.put(user,botState);
    }
    private RestTemplate restTemplate = new RestTemplate();

    private HashMap<Long, MeteoChain> userMeteoChain = new HashMap<>();
    private void initMeteoChain(long user){userMeteoChain.put(user,new MeteoChain());}
    private void setMeteoChainCity(long user,String city){userMeteoChain.get(user).setCityName(city);}
    private void setMeteoChainDelay(long user,int index){userMeteoChain.get(user).setDelayIndex(index);}
    private MeteoChain getMeteoChain(long user){return userMeteoChain.get(user);}

    private HashMap<Long, JokeChain> userJokeChain = new HashMap<>();
    private  void initJokeChain(long user){userJokeChain.put(user,new JokeChain());}
    private  void setJokeChainTitle(long user,String title){userJokeChain.get(user).setTitle(title);}
    private  void setJokeChainContent(long user,String content){userJokeChain.get(user).setContent(content);}
    private  void setJokeChainCategory(long user,String category){userJokeChain.get(user).setCategory(category);}
    private JokeChain getJokeChain(long user){return userJokeChain.get(user);}
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
                        initMeteoChain(userID);
                        sendText(userID,"Mode meteo activé, donnez maintenant une ville pour la recherche");
                        break;
                    default:
                        Pattern pattern = Pattern.compile("quoi", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(message.getText());
                        boolean matchFound = matcher.find();
                        if(matchFound) {
                            sendText(userID,"Feur");
                        }else{
                            sendText(userID,"si tu est perdu fait une /aide pour avoir la liste des commandes");
                        }
                        break;

                    case "poulet":
                        sendText(userID,"🐔");
                        break;
                    case "/blague":
                        Joke joke = restTemplate.getForObject(localURL+"/blague",Joke.class);
                        sendText(userID,formatBlague(joke));
                        break;
                    case "/blaguespecifique":
                        setUserBotState(userID,BotState.WAITING_FOR_JOKE_ID);
                        sendText(userID,"Mode blague specifique activé, donnez maintenant l'id de la blague recherchée");
                        break;
                    case "/ajouterblague":
                        setUserBotState(userID,BotState.WAITING_FOR_JOKE_TITLE);
                        initJokeChain(userID);
                        sendText(userID,"c'est parti pour la création de ta blague, donne moi un titre");
                        break;
                    case "/aide":
                        sendText(userID,"Liste des commandes:\n/meteo - Donne la meteo du jour\n/blague - Donne une blague\n/blaguespecifique - demande une blague en particulier\n/ajouterblague - ajoute une blague petit rigolo\n/aide - affiche la liste des commandes");
                        break;
                }
                break;
            case WAITING_FOR_CITY:
                setMeteoChainCity(userID,messageText);
                sendText(userID,"Ville reçue indiquez le delai de votre prevision 0-aujourd'hui,4- dans 4 jours");
                setUserBotState(userID,BotState.WAITING_FOR_FORECAST_DELAY);
                break;
            case WAITING_FOR_JOKE_ID:
                int id = Integer.parseInt(messageText);
                sendText(userID,grabJoke(id));
                resetUserState(userID);
                break;
            case WAITING_FOR_FORECAST_DELAY:
                setMeteoChainDelay(userID,Integer.parseInt(messageText));
                String bulletin = getMeteoBulletin(getMeteoChain(userID));
                sendText(userID,bulletin);
                resetUserState(userID);
                break;
            case WAITING_FOR_JOKE_TITLE:
                setJokeChainTitle(userID,messageText);
                sendText(userID,"bien, maintenant donne moi le contenu de ta blague");
                setUserBotState(userID,BotState.WAITING_FOR_JOKE_CONTENT);
                break;
            case WAITING_FOR_JOKE_CONTENT:
                setJokeChainContent(userID,messageText);
                sendText(userID,"excellent, j'ai juste besoin de la catégorie et ta blague sera prête");
                setUserBotState(userID,BotState.WAITING_FOR_JOKE_CATEGORY);
                break;
            case WAITING_FOR_JOKE_CATEGORY:
                setJokeChainCategory(userID,messageText);
                resetUserState(userID);
                sendText(userID,AddJoke(getJokeChain(userID)));
                break;
            case WAITING_FOR_JOKE_GRADE:
                float grade = Float.parseFloat(messageText);
                sendText(userID,grabJoke(grade));
                resetUserState(userID);
                break;
        }
    }

    private String AddJoke(JokeChain jokeChain) {
        Random random = new Random();
        Joke joke = new Joke();
        joke.setTitle(jokeChain.getTitle());
        joke.setContent(jokeChain.getContent());
        joke.setCategory(jokeChain.getCategory());
        joke.setGrade(random.nextFloat()*10);
        HttpEntity<Joke> httpEntity = new HttpEntity<>(joke);
        Joke responseJoke = restTemplate.postForObject(localURL+"/blague",httpEntity,Joke.class);
        if(responseJoke == null)
            return "ta blague n'as pas été ajouté";
        return formatBlague(responseJoke);
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
    public String getMeteoBulletin(MeteoChain meteoChain){
        String cityName = meteoChain.getCityName();
        int index = meteoChain.getDelayIndex();
        String url = localURL+"/meteo?cityName="+cityName;
        try{
            ResponseEntity<OpenWeather> responseEntity = restTemplate.getForEntity(url,OpenWeather.class);
            OpenWeather openWeather = responseEntity.getBody();
            List list = openWeather.list.get(8*index);
            cityName = openWeather.city.name;
            String dateTime = list.dt_txt;
            Double temp = list.main.temp;
            String weather = list.weather.get(0).description;
            double windSpeed = list.wind.speed;
            int windDirection = list.wind.deg;
            String bulletin = String.format("Bulletin météo pour %s le %s : \nTempérature : %s°C\nMétéo : %s\nVitesse du vent : %s m/s\nDirection du vent : %s degrés", cityName,dateTime, temp, weather, windSpeed, windDirection);
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
    public String grabJoke(float grade){
        try {
            ResponseEntity<Joke> responseEntity = restTemplate.getForEntity(localURL+"/blague?grade="+grade,Joke.class);
            Joke joke = responseEntity.getBody();
            return formatBlague(joke);
        }catch (HttpClientErrorException errorException){
            return "Ta blague n'existe pas";
        }
    }
}
