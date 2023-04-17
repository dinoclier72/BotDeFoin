package fr.ensim.interop.introrest.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ensim.interop.introrest.model.joke.Joke;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
@RestController
public class JokeController {
    private static final Map<Integer, Joke> listeBlagues = new ConcurrentHashMap<Integer,Joke>();
    public static void loadJokes(){
        //lire les blagues depuis un Json
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Joke[] jokeList = objectMapper.readValue(new File("src/main/resources/jokes.json"), Joke[].class);

            for (Joke joke : jokeList) {
                joke.generateJokeId();
                listeBlagues.put(joke.getId(),joke);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @PostMapping(value="/blague")
    public ResponseEntity<Joke> creerEquipe(@RequestBody Joke blag) {
        blag.generateJokeId();
        listeBlagues.put(blag.getId(), blag);
        return ResponseEntity.ok().body(blag);
    }

    @GetMapping(value = "/blague")
    public ResponseEntity<Joke> recupererBlague(@RequestParam(value = "id", required = false) Integer id,@RequestParam(value = "grade", required = false) Float grade) {

        Random random = new Random();

        //recupération d'une blague specifique
        if (id != null) {
             if(listeBlagues.containsKey(id))
                 return ResponseEntity.ok(listeBlagues.get(id));
            return ResponseEntity.notFound().build();
        }
        //recuperation d'une blague selon son niveau
        if (grade != null) {
            Joke joke;
            do{
                ArrayList<Integer> ids = new ArrayList<>(listeBlagues.keySet());
                int index = random.nextInt(ids.size());
                int randomId = ids.get(index);
                joke = listeBlagues.get(randomId);
            }while (joke.getGrade()<grade);
            return ResponseEntity.ok(joke);
        }
        //recuperation d'une blague aléatoire
        ArrayList<Integer> ids = new ArrayList<>(listeBlagues.keySet());
        int index = random.nextInt(ids.size());
        int randomId = ids.get(index);
        return ResponseEntity.ok(listeBlagues.get(randomId));
    }

}
