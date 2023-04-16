package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.jokemodel.Joke;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JokeController {
    private Map<Integer, Joke> listeBlagues=new ConcurrentHashMap<>();

    @GetMapping(value ="/blague",params = {"id"})
    public ResponseEntity<Joke> recupererBlague(@PathVariable("id") int id) {
        if(listeBlagues.containsKey(id)) {
            return ResponseEntity.ok(listeBlagues.get(id));
        }

        return ResponseEntity.notFound().build();
    }
}
