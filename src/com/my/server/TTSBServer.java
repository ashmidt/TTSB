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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class TTSBServer {

    private ServerSocket serverSocket = null;
    private Socket socket = null;

    private enum Flags {
        SEND, OK, ABK, JSON, CLOSE
    };

    public TTSBServer() {
        try {
            this.serverSocket = new ServerSocket(9889);

        } catch (IOException ex) {
            Logger.getLogger(TTSBServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void StartServer() {
        BufferedReader in;
        PrintWriter out;
        String fromClient;

        try {
            this.serverSocket.accept();

            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while ((fromClient = in.readLine()) != null) {
                if (fromClient.equals("REQ")) {
                    out.println("SEND");
                }

                if (fromClient.contains("CMD")) {
                    String data = fromClient;
                    out.println(data);
                }

                if (fromClient.equals("OK")) {
                    out.println("CLOSE");
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(TTSBServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
