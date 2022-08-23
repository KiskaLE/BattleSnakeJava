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

    private void init(JsonNode game, int numberOfMoves, List<String> snakePath){

    }
    public List<String> simulate(JsonNode game, int numberOfMoves, List<String> snakePath) {
        this.game = game;
        this.me = game.get("you");
        this.snakes = game.get("board").get("snakes");
        List<List<String>> paths = null;
        try {
            paths = generateCombinations(new String[]{"up", "right", "down", "left"}, numberOfMoves);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    private List<List<String>> generateCombinations(String[] moves, int length) throws InterruptedException {
        List<String> movesList = new ArrayList<>(Arrays.asList(moves));
        List<List<String>> list = new ArrayList<>();

        list.add(new ArrayList<>());
        for (int i = 0; i < length; i++) {
            GenerationHelperThread t = new GenerationHelperThread(list, moves, game);
            Thread thread = new Thread(t);
            thread.start();
            thread.join();
            list = new ArrayList<>(t.getCombinations());
        }
        return list;
    }

    private List<List<List<String>>> split(List<List<String>> moves, int number){
        List<List<List<String>>> out = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            List<List<String>> temp = new ArrayList<>();
            int count;
            if (moves.size()%2 == 0){
                count = moves.size()/number;
            }else{
                count = moves.size()/number+1;
            }
            for (int j = 0; j < count; j++) {
                temp.add(moves.get(0));
                moves.remove(0);
            }
            out.add(temp);
        }
        return out;
    }

    public static class GenerationHelperThread implements Runnable{
        List<List<String>> combinations;
        String[] moves;
        JsonNode game;
        GameMap map;
        List<BodyPart> myBodyParts;
        List<List<String>> output;

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
            List<List<String>> output = new ArrayList<>();
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
                int max = movesList.size();
                for (int j = 0; j < max; j++) {
                    List<String> temp = new ArrayList<>(combination);
                    temp.add(movesList.get(j));
                    if (isValidPath(temp)){
                        output.add(temp);
                    }else{
                        output.add(combination);
                    }

                }

            }
            this.output = new ArrayList<>(output);
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

