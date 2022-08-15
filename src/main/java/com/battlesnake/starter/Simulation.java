package com.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Simulation {
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

    //TODO Simulovat hru
    public List<String> simulate(JsonNode game, int numberOfMoves, List<String> snakePath) {
        this.game = game;
        map.makeMap(game);
        me = game.get("you");
        snakes = game.get("board").get("snakes");
        List<List<String>> paths = new ArrayList<>();
        this.myBodyParts = getMySnakeBodyParts();
        paths = generateCombinations(new String[]{"up", "right", "down", "left"}, numberOfMoves);
        Collections.shuffle(paths);
        List<String> longestPath = new ArrayList<>(Arrays.asList("up"));
        for (int j = 0; j < paths.size(); j++) {
            this.map.makeMap(game);
            List<String> path = paths.get(j);
            int headX = this.game.get("you").get("head").get("x").asInt();
            int headY = this.game.get("you").get("head").get("y").asInt();
            boolean isValid = true;
            for (int i = 0; i < path.size(); i++) {
                String x = path.get(i);
                if (isValid) {
                    switch (x) {
                        case "up":
                            if (map.isMoveValid(headX, headY + 1)) {
                                headY++;
                            } else {
                                isValid = false;
                            }
                            break;
                        case "right":
                            if (map.isMoveValid(headX + 1, headY)) {
                                headX++;
                            } else {
                                isValid = false;
                            }
                            break;
                        case "down":
                            if (map.isMoveValid(headX, headY - 1)) {
                                headY--;
                            } else {
                                isValid = false;
                            }
                            break;
                        case "left":
                            if (map.isMoveValid(headX - 1, headY)) {
                                headX--;
                            } else {
                                isValid = false;
                            }
                            break;
                    }
                } else {
                    path.remove(i);
                }

                if (isValid) {
                    this.map = move(headX, headY, this.myBodyParts, map);
                }


            }

            if (longestPath.size() < path.size()) {
                longestPath = path;
            }

        }
        return longestPath;


    }
    //TODO opravit simulovanou mapu

    private List<BodyPart> getMySnakeBodyParts() {
        JsonNode meBodys = me.get("body");
        List<BodyPart> myBody = new ArrayList<>();
        // makes BodyPart class object from JsonNode object
        for (int i = 0; i < meBodys.size(); i++) {
            JsonNode get = meBodys.get(i);
            myBody.add(new BodyPart(get.get("x").asInt(), get.get("y").asInt()));
        }
        return myBody;
    }


    public GameMap move(int x, int y, List<BodyPart> bodyParts, GameMap map) {
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
                    }

                }
            }
        }
        return map;
    }

    /*
    Generates all combinations
     */
    private List<List<String>> generateCombinations(String[] moves, int lenght) {
        List<String> movesList = new ArrayList<>(Arrays.asList(moves));
        List<List<String>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        for (int i = 0; i < lenght; i++) {
            list = helper(list, moves);
        }
        return list;
    }

    private List<List<String>> helper(List<List<String>> combinations, String[] moves) {
        List<List<String>> output = new ArrayList<>();
        for (List<String> combination : combinations) {
            //removes last move to not generate possible move to hit head
            List<String> movesList = new ArrayList<>(Arrays.asList(moves));
            if (combination.size() > 0) {
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
            for (int i = 0; i < max; i++) {
                List<String> temp = new ArrayList<>(combination);
                temp.add(movesList.get(i));
                output.add(temp);
            }

        }
        return output;
    }

    @Override
    public String toString() {
        String s = "";

        return s;
    }
    //TODO return simulaci s nejdelsší životností


}
