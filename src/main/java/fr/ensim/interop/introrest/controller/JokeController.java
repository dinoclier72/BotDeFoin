package fr.ensim.interop.introrest.controller;

import fr.ensim.interop.introrest.jokemodel.Joke;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JokeController {
    private Map<Integer, Joke> listeBlagues=new ConcurrentHashMap<>();
}
