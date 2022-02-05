package com.battlesnake.starter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;

import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.lang.Math;







public class GameMap{
  int height;
  int width;
  int[][] map;
  JsonNode head;
  JsonNode myBodies;
  JsonNode board;
  JsonNode snakes;
  JsonNode hazards;
  JsonNode foods;

  public GameMap(JsonNode head,JsonNode myBodies, JsonNode board,  JsonNode snakes, JsonNode hazards, JsonNode foods) {
    this.head = head;
    this.myBodies = myBodies;
    this.board = board;
    this.snakes = snakes;
    this.hazards = hazards;
    this.foods = foods;
    
    height = board.get("height").asInt();
    width = board.get("width").asInt();
  }

  public void makeMap(){
    map = new int[width][height];
    //reset map
    for (int i = 0; i<width;i++){
      for (int j = 0; j<height; j++){
        map[i][j] = 0;
      }
    }
    //add my body
    for (int i = 0; i < myBodies.size(); i++){
      JsonNode myBody = myBodies.get(i);
      map[myBody.get("x").asInt()][myBody.get("y").asInt()] = 1;
    }
    //add enemy snakes body
    try {
      for (int i = 0; i < snakes.size(); i++){
        JsonNode snakeBody = snakes.get(i).get("body");
        for (int j = 0; j<snakeBody.size(); j++){
          map[snakeBody.get(j).get("x").asInt()][snakeBody.get(j).get("y").asInt()] = 1;
        }
      }
    }catch(Exception e){
      
    }
    //add hazards
    try{
      for (int i = 0; i < hazards.size(); i++){
        JsonNode hazard = hazards.get(i);
        map[hazard.get("x").asInt()][hazard.get("y").asInt()] = 1;
      }
    }catch(Exception e){

    }
  }
  
    

  public boolean isValid(int x, int y) {
    try{
      if (map[x][y] == 1) {
      return false;
    }else {
      return true;
    }
    }catch(Exception e){
      return false;
    }
    
  }
  public JsonNode findFood(){
    if (foods.size()>0) {
      int headX = head.get("x").asInt();
    int headY = head.get("y").asInt();
    int nearest = 0;
    int lenght = 100000000;
         
    for (int i = 0; i<foods.size(); i++){
      JsonNode food = foods.get(i);
      int temp = (Math.abs(food.get("x").asInt() - headX)) + (Math.abs(food.get("y").asInt() - headY));
      if (temp < lenght){
        nearest = i;
        lenght = temp;
      }
    }
    
    return foods.get(nearest);
    }else{
      return null;
    }
  }
    
  
  
}