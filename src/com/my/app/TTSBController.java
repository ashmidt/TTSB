package com.my.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import socketfx.Constants;
import socketfx.FxSocketServer;
import socketfx.SocketListener;

/**
 *
 * @author Alex
 */
public class TTSBController implements Initializable {
    private final static Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    final String GAME_11 = "11";
    final String GAME_21 = "21";
    final String ACTIVE_SERVICE = "serviceFlagActive";
    final String INACTIVE_SERVICE = "serviceFlagInactive";

    final KeyCombination keycombIncreaseLeft = new KeyCodeCombination(KeyCode.UP, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombDecriseLeft = new KeyCodeCombination(KeyCode.DOWN, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombIncreaseRight = new KeyCodeCombination(KeyCode.UP, KeyCodeCombination.ALT_DOWN);
    final KeyCombination keycombDecriseRight = new KeyCodeCombination(KeyCode.DOWN, KeyCodeCombination.ALT_DOWN);
    final KeyCombination keycombReset = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombSwitch = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombScoreTo11 = new KeyCodeCombination(KeyCode.E, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombScoreTo21 = new KeyCodeCombination(KeyCode.T, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombNextGame = new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN);
    final KeyCombination keycombServiceChange = new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN);
    
    @FXML
    private Label counterLeft;
    @FXML
    private Label counterRight;
    @FXML
    private Label playerNameLeft;
    @FXML
    private Label playerNameRight;
    @FXML
    private Label gameCounterLeft;
    @FXML
    private Label gameCounterRight;
    @FXML
    private Pane serviceFlagLeft;
    @FXML
    private Pane serviceFlagRight;
    @FXML
    private BorderPane mainContainer;
    @FXML
    private RadioButton on_offSwitch;
    @FXML
    private Button exitScoreBoard;
    @FXML
    private Circle serverStatus;
    @FXML
    private Label gameTypeLabel;
    @FXML
    private Label ipAddrLabel;

    private int gameServiceNumber = 2;
    private int gameTo = 11;

    private boolean gameCompleted = false;
    
    private boolean isServer = false;

    EventHandler<KeyEvent> eventHandler;
    
    private boolean isConnected;

    private void UpdateName(String name, boolean isForLeftPlayer) {
        Label lbl = isForLeftPlayer ? playerNameLeft: playerNameRight;
        lbl.setText( name.toUpperCase());
    }

    public enum ConnectionDisplayState {
        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }
    
    public enum Cmd {
        LUP, LDOWN, RUP, RDOWN, RESET, SWITCH, SCHANGE, NEXT, GAME11, GAME21, PLLEFT, PLRIGHT
    }

    private FxSocketServer socket;

    private void connect() {
        socket = new FxSocketServer(new FxSocketListener(), 55555, Constants.instance().DEBUG_NONE);
        socket.connect();
    }
    
    private void getIPAddersses() throws SocketException{
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String ipaddr = "";
        
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            
            while (ee.hasMoreElements()){
                InetAddress i = (InetAddress) ee.nextElement();
                
                if(!i.isLoopbackAddress() && !i.isAnyLocalAddress() && i.getHostAddress().contains(".")){
                    System.out.println(i.getHostAddress());
                    ipaddr += i.getHostAddress() + "\n";
                }
            }
        }
        
        ipAddrLabel.setText(ipaddr);
    }
    
    @FXML
    private void handleButtonAction(ActionEvent event) {

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Initializing...");
        ResetBoard(false);
        System.out.println("Add key listener...");
        SetAppKeyHandler();
        
        mainContainer.addEventHandler(KeyEvent.KEY_RELEASED, eventHandler);
        
        Runtime.getRuntime().addShutdownHook(new ShutDownThread());
        
        connect();
        
        try {
            getIPAddersses();
        } catch (SocketException ex) {
            Logger.getLogger(TTSBController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ShutDownThread extends Thread {
        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");    
                }
                socket.shutdown();
            }
        }
    }
    
    class FxSocketListener implements SocketListener {
        @Override
        public void onMessage(String line) {
            if (line != null && !line.equals("")) {
                LOGGER.info(line);
                ProcessRemoteCmd(line.trim());
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
            if (isClosed) {
                isConnected = true;
                connect();
                displayState(ConnectionDisplayState.CONNECTED);
            } else {
            
            }
        }
    }
    
    private void displayState(ConnectionDisplayState state) {
        switch (state) {
            case DISCONNECTED:
                serverStatus.getStyleClass().remove(0);
                serverStatus.getStyleClass().add("serverStatusOff");
                break;
            case CONNECTED:
                serverStatus.getStyleClass().remove(0);
                serverStatus.getStyleClass().add("serverStatusOn");
                break;
            case AUTOCONNECTED:
                break;
        }
    }
    
    protected void SetZeroScore() {
        ResetBoard(true);
    }

    protected void SetGameScore(int i, int gt) {
        gameServiceNumber = i;
        gameTo = gt;
        gameTypeLabel.setText(String.valueOf(gt));
    }

    protected void CheckServiceOrder(int gameService) {
        String classStyle = serviceFlagLeft.getStyleClass().get(0);

        int scoreLeft = Integer.parseInt(counterLeft.getText());
        int scoreRight = Integer.parseInt(counterRight.getText());

        int total = (scoreLeft + scoreRight) % gameService;

        if (total == 0) {
            if (classStyle.equalsIgnoreCase(ACTIVE_SERVICE)) {
                ChangeServiceColor(ACTIVE_SERVICE, INACTIVE_SERVICE);
            } else {
                ChangeServiceColor(INACTIVE_SERVICE, ACTIVE_SERVICE);
            }
        }
    }
    
    private void ChangeServiceColor(String s1, String s2) {
        serviceFlagRight.getStyleClass().remove(0);
        serviceFlagLeft.getStyleClass().remove(0);
        serviceFlagRight.getStyleClass().add(s1);
        serviceFlagLeft.getStyleClass().add(s2);
    }

    private void SetGameCounter(Label gameCounter) {
        int counter = Integer.parseInt(gameCounter.getText()) + 1;
        
        gameCounter.setText(String.valueOf(counter));
    }

    private void ResetBoard(boolean resetScoreCounter) {
        if (resetScoreCounter) {
            counterLeft.setText("0");
            counterRight.setText("0");
            gameCompleted = false;
        } else {
            counterLeft.setText("0");
            counterRight.setText("0");
            gameCounterLeft.setText("0");
            gameCounterRight.setText("0");
            playerNameLeft.setText("Player1");
            playerNameRight.setText("Player2");
            gameCompleted = false;
            ChangeServiceColor(INACTIVE_SERVICE, ACTIVE_SERVICE);
        }
    }

    public void IncreaseCounter(boolean isForLeftPlayer) {
        Label lbl;
        int scoreP1 = Integer.parseInt(counterLeft.getText());
        int scoreP2 = Integer.parseInt(counterRight.getText());
        int counter = 0;

        if (isForLeftPlayer) {
            scoreP1 = Integer.parseInt(counterLeft.getText()) + 1;
            lbl = counterLeft;
            counter = scoreP1;
        } else {
            scoreP2 = Integer.parseInt(counterRight.getText()) + 1;
            lbl = counterRight;
            counter = scoreP2;
        }

        if (counter < gameTo) {
            lbl.setText(String.valueOf(counter));
        } else {
            if ((scoreP1 - scoreP2) == 0) {
                System.out.println("Score tie");
                lbl.setText(String.valueOf(counter));
            } else if (Math.abs((scoreP1 - scoreP2)) > 1) {
                System.out.println("Score stop");
                lbl.setText(String.valueOf(counter));
                gameCompleted = true;
                if (scoreP1 > scoreP2) {
                    SetGameCounter(gameCounterLeft);
                } else {
                    SetGameCounter(gameCounterRight);
                }
            } else {
                if (!gameCompleted) {
                    lbl.setText(String.valueOf(counter));
                } else {
                    if (scoreP1 > scoreP2) {
                        SetGameCounter(gameCounterLeft);
                    } else {
                        SetGameCounter(gameCounterRight);
                    }
                }
            }
        }
    }

    public void DecriseCounter(boolean isForLeftPlayer) {
        Label lbl = isForLeftPlayer ? counterLeft : counterRight;

        int counter = Integer.parseInt(lbl.getText());
        if (counter > 0) {
            counter--;
            lbl.setText(String.valueOf(counter));
        }
    }

    private void SwitchSides() {
        String oldSideLeft;

        oldSideLeft = playerNameLeft.getText();
        playerNameLeft.setText(playerNameRight.getText());
        playerNameRight.setText(oldSideLeft);

        oldSideLeft = gameCounterLeft.getText();
        gameCounterLeft.setText(gameCounterRight.getText());
        gameCounterRight.setText(oldSideLeft);

        oldSideLeft = counterLeft.getText();
        counterLeft.setText(counterRight.getText());
        counterRight.setText(oldSideLeft);
    }

    private void ProcessRemoteCmd(String data){
        if (data != null && data.equalsIgnoreCase(Cmd.LUP.name())) {
                //System.out.println(event.getText());               
                if (!gameCompleted) {
                    IncreaseCounter(true);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (data != null && data.equalsIgnoreCase(Cmd.LDOWN.name())) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    DecriseCounter(true);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (data != null && data.equalsIgnoreCase(Cmd.RUP.name())) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    IncreaseCounter(false);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (data != null && data.equalsIgnoreCase(Cmd.RDOWN.name())) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    DecriseCounter(false);
                    CheckServiceOrder(gameServiceNumber);
                }
            }
            
            if (data != null && data.equalsIgnoreCase(Cmd.RESET.name())) {
                ResetBoard(false);
            }

            if (data != null && data.equalsIgnoreCase(Cmd.SWITCH.name())) {
                SwitchSides();
            }

            if (data != null && data.equalsIgnoreCase(Cmd.GAME11.name())) {
                SetGameScore(2, 11);
            }

            if (data != null && data.equalsIgnoreCase(Cmd.GAME21.name())) {
                SetGameScore(5, 21);
            }

            if (data != null && data.equalsIgnoreCase(Cmd.NEXT.name())) {
                SetZeroScore();
            }
            
            if(data != null && data.equalsIgnoreCase(Cmd.SCHANGE.name())){
                ChangeService();
            }
            
            if(data != null && data.startsWith(Cmd.PLLEFT.name())){
                String[] nameArr = data.split(":");
                String name = nameArr[1];
                
                UpdateName(name, true);
            }
            
            if(data != null && data.startsWith(Cmd.PLRIGHT.name())){
                String[] nameArr = data.split(":");
                String name = nameArr[1];
                
                UpdateName(name, false);
            }
    }
    
    private void ProcessEvents(KeyEvent event){
        if (keycombIncreaseLeft.match(event)) {
                //System.out.println(event.getText());               
                if (!gameCompleted) {
                    IncreaseCounter(true);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (keycombDecriseLeft.match(event)) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    DecriseCounter(true);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (keycombIncreaseRight.match(event)) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    IncreaseCounter(false);
                    CheckServiceOrder(gameServiceNumber);
                }
            }

            if (keycombDecriseRight.match(event)) {
                if (!gameCompleted) {
                    //System.out.println(event.getText());
                    DecriseCounter(false);
                    CheckServiceOrder(gameServiceNumber);
                }
            }
            
            if (keycombReset.match(event)) {
                ResetBoard(false);
            }

            if (keycombSwitch.match(event)) {
                SwitchSides();
            }

            if (keycombScoreTo11.match(event)) {
                SetGameScore(2, 11);
            }

            if (keycombScoreTo21.match(event)) {
                SetGameScore(5, 21);
            }

            if (keycombNextGame.match(event)) {
                SetZeroScore();
            }
            
            if(keycombServiceChange.match(event)){
                ChangeService();
            }
    }
    
    private void SetAppKeyHandler() {
        eventHandler = (KeyEvent event) -> {
            //System.out.println(event.getCode());
            ProcessEvents(event);
        };
    }

    private void ChangeService() {
        if(serviceFlagLeft.getStyleClass().get(0).equalsIgnoreCase(ACTIVE_SERVICE)){
            ChangeServiceColor(ACTIVE_SERVICE, INACTIVE_SERVICE);
        }else{
            ChangeServiceColor(INACTIVE_SERVICE, ACTIVE_SERVICE);
        }
    }
}