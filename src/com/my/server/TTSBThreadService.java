/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.my.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author Alex
 */
public class TTSBThreadService extends Thread {

    private Socket socket = null;

    private enum Flags {
        SEND, OK, CMD, REQ, CLOSE
    };

    public TTSBThreadService(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String fromClient;
            System.out.println("Server started...: ");
            
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Client: " + fromClient);
                Flags flag = Flags.valueOf(fromClient);

                if (fromClient.equals("JSON")) {
                    out.println("SEND");
                }

                if (fromClient.contains("CMD")) {
                    //String data = processData(fromClient);
                    //out.println(data);
                    out.println("DONE");
                }

                if (fromClient.equals("OK")) {
                    out.println("CLOSE");
                }
            }

        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
