/*
 * Created by Denis Lukashov
 * Copyright (c) 2019. Styx-man. All rights reserved.
 */

package com.styxman.game.snake;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The snake Classic Game.
 *
 * @author Denis_Lukashov
 */
public class Game extends Application {

    /**
     * Directions where the Snake can move.
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static final int BLOCK_SIZE = 25;
    public static final int APP_WIDTH = 30 * BLOCK_SIZE;
    public static final int APP_HEIGHT = 20 * BLOCK_SIZE;

    // Default direction
    private Direction direction = Direction.RIGHT;

    // Flag to insure that snake does not move to the different directions at the same time
    private boolean moved = false;

    // Application is running (or not)
    private boolean running = false;

    // Animation
    private Timeline timeline = new Timeline();

    //snake body. Going to consists of rectangles
    private ObservableList<Node> snake;

    private Parent createContent() {
        // Create and set the size of the game window (pane)
        Pane root = new Pane();
        root.setPrefSize(APP_WIDTH, APP_HEIGHT);

        // Prepare body for the snake (can add and remove parts)
        Group snakeBody = new Group();
        snake = snakeBody.getChildren();

        // Create and setup food for the snake
        Rectangle food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);

        food.setFill(Color.ROYALBLUE);
        // Part (/ BLOCK_SIZE * BLOCK_SIZE) for normalizing for the cornet value
        food.setTranslateX((int) (Math.random() * (APP_WIDTH - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (APP_HEIGHT - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);

        // Single frame in animation. Gonna be indefinite and recalls every 0.15 seconds
        KeyFrame frame = new KeyFrame(Duration.seconds(0.15), event -> {

            // If the game is not running - do nothing
            if (!running) {
                return;
            }

            // Expression becomes true when there are at list 2 or more blocks in the body
            boolean toRemove = snake.size() > 1;

            // And if it is, remove the tail (last block), and if not just get the head (first element) of the snake
            // The idea: to get tail and move it to the front, so it becomes the head
            // (it's much easier to move the tail to the head)
            Node tail = toRemove ? snake.remove(snake.size() - 1) : snake.get(0);

            //In case we eat the food, we remember old tail coordinates
            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();

            //Movements
            switch (direction) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() + BLOCK_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX() - BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX() + BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
            }

            // Now we can change direction
            moved = true;

            //We remove one block and then put one block back. We put the tail in front
            if (toRemove) {
                snake.add(0, tail);
            }

            // Collision detection

            // Checking is the snake hits its own body, and if does - restart the game
            // (tail - the head now)
            for (Node rect : snake) {
                if (rect != tail && tail.getTranslateX() == rect.getTranslateX()
                        && tail.getTranslateY() == rect.getTranslateY()) {
                    restartGame();
                    break;
                }
            }

            // Collision against the wall (screen)
            if (tail.getTranslateX() < 0 || tail.getTranslateX() >= APP_WIDTH
                    || tail.getTranslateY() < 0 || tail.getTranslateY() >= APP_HEIGHT) {
                restartGame();
            }

            // Collision with the food
            if (tail.getTranslateX() == food.getTranslateX()
                    && tail.getTranslateY() == food.getTranslateY()) {
                food.setTranslateX((int) (Math.random() * (APP_WIDTH - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
                food.setTranslateY((int) (Math.random() * (APP_HEIGHT - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);

                // Adding the rectangle to the body of the snake
                Rectangle rect = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
                rect.setTranslateX(tailX);
                rect.setTranslateY(tailY);

                snake.add(rect);
            }
        });

        // We gonna run one frame indefinite times
        timeline.getKeyFrames().add(frame);
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Our Nodes now should be displayed
        // (snakeBody is a list of snake nodes)
        root.getChildren().addAll(food, snakeBody);
        return root;
    }

    // Easy helper method witch stops the game and restart it again
    private void restartGame() {
        stopGame();
        startGame();
    }

    private void stopGame() {
        running = false;
        timeline.stop();
        snake.clear();
    }

    private void startGame() {
        direction = Direction.RIGHT;
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        snake.add(head);
        timeline.play();
        running = true;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createContent());

        // Input handling
        scene.setOnKeyPressed(event -> {
            if (!moved) {
                return;
            }

            switch (event.getCode()) {
                case UP:
                    if (direction != Direction.DOWN) {
                        direction = Direction.UP;
                    }
                    break;
                case DOWN:
                    if (direction != Direction.UP) {
                        direction = Direction.DOWN;
                    }
                    break;
                case RIGHT:
                    if (direction != Direction.LEFT) {
                        direction = Direction.RIGHT;
                    }
                    break;
                case LEFT:
                    if (direction != Direction.RIGHT) {
                        direction = Direction.LEFT;
                    }
                    break;
            }

            moved = false;
        });

        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}