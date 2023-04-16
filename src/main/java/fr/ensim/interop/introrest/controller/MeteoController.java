package fr.ensim.interop.introrest.controller;


import fr.ensim.interop.introrest.model.meteo.City;
import fr.ensim.interop.introrest.model.meteo.OpenWeather;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MeteoController {
    @Value("${open.weather.geocoding.api.url}")
    private String cityURL;
    @Value("${open.weather.api.token}")
    private String token;
    @Value("${open.weather.api.url}")
    private String meteoURL;
    @GetMapping(value = "/meteo", params = {"cityName"})
    public ResponseEntity<OpenWeather> meteo(@RequestParam("cityName") String cityName){
        RestTemplate restTemplate = new RestTemplate();
        //On utilise l'api Geocoding pour recupérer les coordonnées
        City[] cities = restTemplate.getForObject(cityURL,City[].class,cityName,token);
        if(cities.length == 0){
            return ResponseEntity.notFound().build();
        }
        City city = cities[0];
        OpenWeather openWeather = restTemplate.getForObject(meteoURL,OpenWeather.class, String.valueOf(city.lat),String.valueOf(city.lon),token);
        return ResponseEntity.ok().body(openWeather);
    }
}
