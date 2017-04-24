package com.my.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.my.server.TTSBThreadService;
import java.io.IOException;
import java.net.ServerSocket;
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
       }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void initServer(){
        try (ServerSocket serverSocket = new ServerSocket(9888)) {

            //serverSocket.setSoTimeout(10 * 1000);
            //while (true) {
                new TTSBThreadService(serverSocket.accept()).start();
            //}

        } catch (IOException e) {
            System.out.println("Could not listen on port 9889: " + e.getMessage());
            System.exit(-1);
        }
    }
}
