package com.battlesnake.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import java.util.*;
import static spark.Spark.*;

/**
 * This is a simple Battlesnake server written in Java.
 * <p>
 * For instructions see
 * https://github.com/BattlesnakeOfficial/starter-snake-java/README.md
 */
public class Snake {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Handler HANDLER = new Handler();
    private static final Logger LOG = LoggerFactory.getLogger(Snake.class);

    /**
     * Main entry point.
     *
     * @param args are ignored.
     */
    public static void main(String[] args) {
        String port = System.getProperty("PORT");
        if (port == null) {
            LOG.info("Using default port: {}", port);
            port = "8080";
        } else {
            LOG.info("Found system provided port: {}", port);
        }
        port(Integer.parseInt(port));
        get("/", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
        post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
    }

    /**
     * Handler class for dealing with the routes set up in the main method.
     */
    public static class Handler {

        GameMap gameMap;
        Simulation simulation;
        List<String> snakePath;
        /**
         * For the start/end request
         */
        private static final Map<String, String> EMPTY = new HashMap<>();

        /**
         * Generic processor that prints out the request and response from the methods.
         *
         * @param req
         * @param res
         * @return
         */
        public Map<String, String> process(Request req, Response res) {
            try {
                JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
                String uri = req.uri();
                LOG.info("{} called with: {}", uri, req.body());
                Map<String, String> snakeResponse;
                if (uri.equals("/")) {
                    snakeResponse = index();
                } else if (uri.equals("/start")) {
                    snakeResponse = start(parsedRequest);
                } else if (uri.equals("/move")) {
                    snakeResponse = move(parsedRequest);
                } else if (uri.equals("/end")) {
                    snakeResponse = end(parsedRequest);
                } else {
                    throw new IllegalAccessError("Strange call made to the snake: " + uri);
                }

                LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));

                return snakeResponse;
            } catch (JsonProcessingException e) {
                LOG.warn("Something went wrong!", e);
                return null;
            }
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * <p>
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @return a response back to the engine containing the Battlesnake setup
         * values.
         */
        public Map<String, String> index() {
            Map<String, String> response = new HashMap<>();
            response.put("apiversion", "1");
            response.put("author", "Kiska");
            response.put("color", "#ffbf00");
            response.put("head", "evil");
            response.put("tail", "coffee");
            return response;
        }

        /**
         * This method is called everytime your Battlesnake is entered into a game.
         * <p>
         * Use this method to decide how your Battlesnake is going to look on the board.
         *
         * @param startRequest a JSON data map containing the information about the game
         *                     that is about to be played.
         * @return responses back to the engine are ignored.
         */

        public Map<String, String> start(JsonNode startRequest) {
            LOG.info("START");
            gameMap = new GameMap();
            simulation = new Simulation();
            snakePath = new ArrayList<>();
            return EMPTY;
        }

        /**
         * This method is called on every turn of a game. It's how your snake decides
         * where to move.
         * <p>
         * Use the information in 'moveRequest' to decide your next move. The
         * 'moveRequest' variable can be interacted with as
         * com.fasterxml.jackson.databind.JsonNode, and contains all of the information
         * about the Battlesnake board for each move of the game.
         * <p>
         * For a full example of 'json', see
         * https://docs.battlesnake.com/references/api/sample-move-request
         *
         * @param moveRequest JsonNode of all Game Board data as received from the
         *                    Battlesnake Engine.
         * @return a Map<String,String> response back to the engine the single move to
         * make. One of "up", "down", "left" or "right".
         */
        public Map<String, String> move(JsonNode moveRequest) {

            try {
                LOG.info("Data: {}", JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(moveRequest));
            } catch (JsonProcessingException e) {
                LOG.error("Error parsing payload", e);
            }

            JsonNode body = moveRequest.get("you").get("body");
            JsonNode head = moveRequest.get("you").get("head");
            JsonNode board = moveRequest.get("board");
            JsonNode snakes = moveRequest.get("board").get("snakes");
            JsonNode hazards = moveRequest.get("board").get("hazards");
            JsonNode foods = moveRequest.get("board").get("food");

            ArrayList<String> possibleMoves = new ArrayList<>(Arrays.asList("up", "down", "left", "right"));
            snakePath = simulation.simulate(moveRequest, 9, snakePath);
            gameMap.makeMap(moveRequest);
            avoid(gameMap, head, possibleMoves);
            String move;
            if (!snakePath.isEmpty()) {
                move = snakePath.get(0);
            } else {
                move = "up";
            }

            LOG.info("MOVES {}", Arrays.toString(snakePath.toArray()));
            LOG.info("MOVE {}", move);
            Map<String, String> response = new HashMap<>();
            response.put("move", move);
            snakePath.remove(0);
            return response;
        }

        // use map to detemine if move is valid
        public void avoid(GameMap gameMap, JsonNode head, ArrayList<String> possibleMoves) {

            int headX = head.get("x").asInt();
            int headY = head.get("y").asInt();

            if (gameMap.isMoveValid(headX + 1, headY) == false) {
                possibleMoves.remove("right");
            }
            if (gameMap.isMoveValid(headX - 1, headY) == false) {
                possibleMoves.remove("left");
            }
            if (gameMap.isMoveValid(headX, headY + 1) == false) {
                possibleMoves.remove("up");
            }
            if (gameMap.isMoveValid(headX, headY - 1) == false) {
                possibleMoves.remove("down");
            }
        }

        public void foodTarget(JsonNode head, GameMap gameMap, ArrayList<String> possibleMoves) {
            try {
                int headX = head.get("x").asInt();
                int headY = head.get("y").asInt();
                JsonNode foodLocation = gameMap.findFood();
                int lenght = Math.abs(headX - foodLocation.get("x").asInt()) + Math.abs(headY - foodLocation.get("y").asInt());

                if (Math.abs(headX + 1 - foodLocation.get("x").asInt()) + Math.abs(headY - foodLocation.get("y").asInt()) > lenght) {
                    possibleMoves.remove("right");
                }
                if (Math.abs(headX - 1 - foodLocation.get("x").asInt()) + Math.abs(headY - foodLocation.get("y").asInt()) > lenght) {
                    possibleMoves.remove("left");
                }
                if (Math.abs(headX - foodLocation.get("x").asInt()) + Math.abs(headY + 1 - foodLocation.get("y").asInt()) > lenght) {
                    possibleMoves.remove("up");
                }
                if (Math.abs(headX - foodLocation.get("x").asInt()) + Math.abs(headY - 1 - foodLocation.get("y").asInt()) > lenght) {
                    possibleMoves.remove("down");
                }
            } catch (Exception e) {

            }


        }

        /**
         * This method is called when a game your Battlesnake was in ends.
         * <p>
         * It is purely for informational purposes, you don't have to make any decisions
         * here.
         *
         * @param endRequest a map containing the JSON sent to this snake. Use this data
         *                   to know which game has ended
         * @return responses back to the engine are ignored.
         */
        public Map<String, String> end(JsonNode endRequest) {
            LOG.info("END");
            return EMPTY;
        }
    }

}
