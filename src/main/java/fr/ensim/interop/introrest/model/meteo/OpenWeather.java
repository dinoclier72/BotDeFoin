package fr.ensim.interop.introrest.model.meteo; 
import java.util.ArrayList;
public class OpenWeather{
    public String cod;
    public int message;
    public int cnt;
    public ArrayList<List> list;
    public WeatherCityInfo city;
}
