package com.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.CollationElementIterator;
import java.util.*;

//TODO add multithreading

public class Simulation{


    public static class BodyPart {
        int x;
        int y;

        public BodyPart(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setLocation(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    private JsonNode game;
    private JsonNode me;
    private JsonNode snakes;
    private GameMap map;
    private List<BodyPart> myBodyParts;
    private List<String> moves;

    public Simulation() {
        moves = new ArrayList<>();
        map = new GameMap();
    }


    public List<String> simulate(JsonNode game, int numberOfMoves, int cores) {
        this.game = game;
        this.me = game.get("you");
        this.snakes = game.get("board").get("snakes");
        List<List<String>> paths = null;
        paths = generateCombinations(new String[]{"up", "right", "down", "left"}, numberOfMoves, cores);
        List<String> longestPath = new ArrayList<>();
        Collections.shuffle(paths);
        for (int i = 0; i < paths.size(); i++) {
            if (i == 0){
                longestPath = paths.get(i);
            }else{
                if (longestPath.size()<paths.get(i).size()){
                    longestPath = paths.get(i);
                }
            }
        }
        return longestPath;
    }

    /*
    Generates all combinations
     */
    private List<List<String>> generateCombinations(String[] moves, int length, int cores) {
        List<List<String>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        int numberOfThreads = cores;
        for (int i = 0; i < length; i++) {
            if (list.size()<numberOfThreads){
                GenerationHelperThread t = new GenerationHelperThread(list, moves, game);
                Thread thread = new Thread(t);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.out.println("error");
                }
                list = new ArrayList<>(t.getCombinations());
            }else{
                List<GenerationHelperThread> helpers = new ArrayList<>();
                List<Thread> threads = new ArrayList<>();
                List<List<String>>[] movesArrays = split(list, numberOfThreads);
                for (int j = 0; j < movesArrays.length; j++) {
                    List<List<String>> get = movesArrays[j];
                    GenerationHelperThread thread = new GenerationHelperThread(get, moves, game);
                    helpers.add(thread);
                    threads.add(new Thread(thread));
                }
                for (int j = 0; j < threads.size(); j++) {
                    threads.get(j).start();
                }
                for (int j = 0; j < threads.size(); j++) {
                    try {
                        threads.get(j).join();
                    } catch (InterruptedException e) {
                        System.out.println("error");
                    }
                }
                for (int j = 0; j < helpers.size(); j++) {
                    List<List<String>> temp = helpers.get(j).getCombinations();
                    list = new ArrayList<>();
                    for (int k = 0; k < helpers.get(j).getCombinations().size(); k++) {
                        list.add(helpers.get(j).getCombinations().get(k));
                    }
                }
            }

        }
        return list;
    }

    private List<List<String>>[] split(List<List<String>> moves, int number){
        int min = 0;
        int max = moves.size();
        int result = max/number;
        List<List<String>>[] out = new List[number];
        boolean isOdd;
        if (moves.size()%number == 0){
            isOdd = false;
        }else{
            isOdd = true;
        }
        for (int i = 0; i < number; i++) {
            if (isOdd && i == 0){
                min = (max - (result))-1;
            }else{
                min = max - (result);
            }

            out[i] = new ArrayList<>();
            List<List<String>> get = out[i];
            for (int j = min; j < max; j++) {
                get.add(moves.get(j));
            }
            max = min;
        }
        return out;
    }

    public static class GenerationHelperThread implements Runnable{
        List<List<String>> combinations;
        String[] moves;
        JsonNode game;
        GameMap map;
        List<BodyPart> myBodyParts;
        List<List<String>> output = new ArrayList<>();

        @Override
        public void run() {
            generationHelper(combinations, moves);
        }

        public GenerationHelperThread(List<List<String>> combinations, String[] moves, JsonNode game) {
            this.combinations = combinations;
            this.moves = moves;
            this.game = game;
            this.map = new GameMap();
            this.myBodyParts = getMySnakeBodyParts();
        }

        public List<List<String>> getCombinations(){
            return output;
        }

        private void generationHelper(List<List<String>> combinations, String[] moves) {
            List<List<String>> out = new ArrayList<>();
            List<String> longest = new ArrayList<>();
            for (int i = 0; i<combinations.size(); i++) {
                List<String> combination = combinations.get(i);
                List<String> movesList = new ArrayList<>(Arrays.asList(moves));
                //removes last move to not generate possible move to hit head
                if (combination.size() > 1) {
                    switch (combination.get(combination.size() - 1)) {
                        case "up":
                            movesList.remove("down");
                            break;
                        case "right":
                            movesList.remove("left");
                            break;
                        case "down":
                            movesList.remove("up");
                            break;
                        case "left":
                            movesList.remove("right");
                            break;
                    }
                }
                boolean isValid = false;
                for (int j = 0; j < movesList.size(); j++) {
                    List<String> temp = new ArrayList<>(combination);
                    temp.add(movesList.get(j));


                    if (isValidPath(temp)){
                        out.add(temp);
                        isValid = true;
                    }
                }
                if (!isValid && longest.size() < combination.size()){
                    longest = combination;
                }
            }
            if (out.isEmpty()){
                out.add(longest);
            }
            for (int i = 0; i < out.size(); i++) {
                output.add(out.get(i));
            }
        }

        private List<BodyPart> getMySnakeBodyParts() {
            JsonNode myBodies = game.get("you").get("body");
            List<BodyPart> myBody = new ArrayList<>();
            // makes BodyPart class object from JsonNode object
            for (int i = 0; i < myBodies.size(); i++) {
                JsonNode get = myBodies.get(i);
                myBody.add(new BodyPart(get.get("x").asInt(), get.get("y").asInt()));
            }
            return myBody;
        }

        private GameMap move(int x, int y, List<BodyPart> bodyParts, GameMap map) {
            int[] lastPartLocation = new int[2];
            int[] partLocation = new int[2];
            for (int i = 0; i < bodyParts.size(); i++) {
                BodyPart part = bodyParts.get(i);
                if (i == 0) {
                    lastPartLocation[0] = part.x;
                    lastPartLocation[1] = part.y;
                    part.setLocation(x, y);
                    map.changeMapAtLocation(x, y, 1);
                } else {
                    partLocation[0] = part.x;
                    partLocation[1] = part.y;
                    part.setLocation(lastPartLocation[0], lastPartLocation[1]);
                    lastPartLocation = partLocation;
                    map.changeMapAtLocation(x, y, 1);
                    if (i == bodyParts.size() - 1) {
                        if (!map.isFood(x, y)){
                            map.changeMapAtLocation(partLocation[0], partLocation[1], 0);
                        }else{
                            this.myBodyParts.add(new BodyPart(lastPartLocation[0], lastPartLocation[1]));
                            break;
                        }

                    }
                }
            }
            return map;
        }

        private boolean isValidPath(List<String> path){
            this.map.makeMap(game);
            this.myBodyParts = getMySnakeBodyParts();
            int headX = this.game.get("you").get("head").get("x").asInt();
            int headY = this.game.get("you").get("head").get("y").asInt();

            for (int i = 0; i < path.size(); i++) {
                String x = path.get(i);
                if (i< path.size()-1) {
                    switch (x) {
                        case "up":
                            headY++;
                            break;
                        case "right":
                            headX++;
                            break;
                        case "down":
                            headY--;
                            break;
                        case "left":
                            headX--;
                            break;
                    }
                    this.map = move(headX, headY, this.myBodyParts, map);
                }else{
                    switch (x) {
                        case "up":
                            if (map.isMoveValid(headX, headY + 1)) {
                                return true;
                            } else {
                                return false;
                            }

                        case "right":
                            if (map.isMoveValid(headX + 1, headY)) {
                                return true;
                            } else {
                                return false;
                            }

                        case "down":
                            if (map.isMoveValid(headX, headY - 1)) {
                                return true;
                            } else {
                                return false;
                            }

                        case "left":
                            if (map.isMoveValid(headX - 1, headY)) {
                                return true;
                            } else {
                                return false;
                            }

                    }
                }
            }
            return false;
        }

    }


}

