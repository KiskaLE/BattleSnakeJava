package com.battlesnake.starter;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;


public class GameMap {
    int height;
    int width;
    int[][] map;
    JsonNode head;
    JsonNode myBodies;
    JsonNode board;
    JsonNode snakes;
    JsonNode hazards;
    JsonNode foods;

    //public GameMap(JsonNode head,JsonNode myBodies, JsonNode board,  JsonNode snakes, JsonNode hazards, JsonNode foods) {
    //  this.head = head;
    //  this.myBodies = myBodies;
    //  this.board = board;
    //  this.snakes = snakes;
    //  this.hazards = hazards;
    //  this.foods = foods;

    //  height = board.get("height").asInt();
    //  width = board.get("width").asInt();
    //}

    public GameMap() {

    }

    public GameMap(int[][] map) {
        this.map = map;
    }

    public int[][] getMap() {
        return map;
    }

    public void makeMap(JsonNode game) {
        head = game.get("you").get("head");
        myBodies = game.get("you").get("body");
        board = game.get("board");
        snakes = board.get("snakes");
        hazards = board.get("hazards");
        foods = board.get("food");
        height = board.get("height").asInt();
        width = board.get("width").asInt();
        map = new int[width][height];
        resetMap();
        addMyBody();
        addEnemySnakes();
        addHazards();
        addFood();
    }

    public void changeMapAtLocation(int x, int y, int type) {
        try {
            map[x][y] = type;

        } catch (Exception e) {

        }
    }

    private void addHazards() {
        try {
            for (int i = 0; i < hazards.size(); i++) {
                JsonNode hazard = hazards.get(i);
                map[hazard.get("x").asInt()][hazard.get("y").asInt()] = 1;
            }
        } catch (Exception e) {

        }
    }

    private void addEnemySnakes() {
        try {
            for (int i = 0; i < snakes.size(); i++) {
                JsonNode snakeBody = snakes.get(i).get("body");
                for (int j = 0; j < snakeBody.size(); j++) {
                    map[snakeBody.get(j).get("x").asInt()][snakeBody.get(j).get("y").asInt()] = 1;
                }
            }
        } catch (Exception e) {

        }
    }
    private void addFood(){
        try{
            for (int i = 0; i < foods.size(); i++) {
                JsonNode food = foods.get(i);
                changeMapAtLocation(food.get("x").asInt(), food.get("y").asInt(), 2);
            }
        }catch (Exception e){

        }
    }

    private void resetMap() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                map[i][j] = 0;
            }
        }
    }

    private void addMyBody() {
        for (int i = 0; i < myBodies.size(); i++) {
            JsonNode myBody = myBodies.get(i);
            map[myBody.get("x").asInt()][myBody.get("y").asInt()] = 1;
        }
    }


    public boolean isMoveValid(int x, int y) {
        return isNotAtLocation(x, y, 1);

    }
    public boolean isFood(int x, int y){
        return isNotAtLocation(x, y, 2);
    }

    private boolean isNotAtLocation(int x, int y, int type){
        try {
            if (map[x][y] == type) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
    public JsonNode findFood() {
        if (foods.size() > 0) {
            int headX = head.get("x").asInt();
            int headY = head.get("y").asInt();
            int nearest = 0;
            int minimalDistance = 0;

            for (int i = 0; i < foods.size(); i++) {
                JsonNode food = foods.get(i);
                int distance = (Math.abs(food.get("x").asInt() - headX)) + (Math.abs(food.get("y").asInt() - headY));
                if (i == 0) {
                    minimalDistance = distance;
                }
                if (distance < minimalDistance) {
                    nearest = i;
                    minimalDistance = distance;
                }
            }

            return foods.get(nearest);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(map);
    }
}