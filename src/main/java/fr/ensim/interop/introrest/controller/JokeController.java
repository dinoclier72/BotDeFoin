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
            Joke[] jokeList = objectMapper.readValue(new File("jokes.json"), Joke[].class);

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
            Collection<Joke> jokeCollection = listeBlagues.values();
            for(Joke joke : jokeCollection){
                if (joke.getGrade() < grade){
                    jokeCollection.remove(joke);
                }

            }
            if(jokeCollection.size()==0)
                return ResponseEntity.notFound().build();
            int index = random.nextInt(jokeCollection.size());
            return ResponseEntity.ok(jokeCollection.toArray()[index]);

        }
        //recuperation d'une blague aléatoire
        ArrayList<Integer> ids = new ArrayList<>(listeBlagues.keySet());
        int index = random.nextInt(ids.size());
        int randomId = ids.get(index);
        return ResponseEntity.ok(listeBlagues.get(randomId));
    }

}
