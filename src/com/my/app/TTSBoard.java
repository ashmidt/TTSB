package com.my.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Alex
 */
public class TTSBoard extends Application {

    final KeyCombination keycombExit = new KeyCodeCombination(KeyCode.X, KeyCodeCombination.CONTROL_DOWN);
    EventHandler<KeyEvent> eventHandler;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("scoreboard.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);

        eventHandler = (KeyEvent event) -> {
            if (keycombExit.match(event)) {
                Platform.exit();
            }
        };

        scene.addEventHandler(KeyEvent.KEY_RELEASED, eventHandler);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
