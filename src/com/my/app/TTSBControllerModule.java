package com.my.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.my.games.TTGame;
import com.my.games.TableTennisGame;
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
public class TTSBControllerModule implements Initializable {
    private final static Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
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

    private boolean isServer = false;

    EventHandler<KeyEvent> eventHandler;

    private boolean isConnected;

    private TTGame ttgame = new TTGame();

    private void UpdatePlayerScore(TTGame.PlayerSide side, int score) {
        switch (side) {
            case LEFT:
                counterLeft.setText(String.valueOf(score));
            case RIGHT:
                counterRight.setText(String.valueOf(score));
        }
    }

    private void UpdateGameScore(TTGame.PlayerSide side, int score) {
        switch (side) {
            case LEFT:
                gameCounterLeft.setText(String.valueOf(score));
            case RIGHT:
                gameCounterRight.setText(String.valueOf(score));
        }
    }

    private void UpdatePlayerName(TableTennisGame.PlayerSide side, String playerName) {
        switch (side) {
            case LEFT:
                playerNameLeft.setText(String.valueOf(playerName.toUpperCase().trim()));
                break;
            case RIGHT:
                playerNameRight.setText(String.valueOf(playerName.toUpperCase().trim()));
                break;
            default:
                throw new AssertionError(side.name());
        }
    }

    private void UpdateGameType(int gameType) {
        gameTypeLabel.setText(String.valueOf(gameType));
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

    private void getIPAddersses() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String ipaddr = "";

        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();

            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();

                if (!i.isLoopbackAddress() && !i.isAnyLocalAddress() && i.getHostAddress().contains(".")) {
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
        UpdateUI();
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
                ProcessRemoteCmd(Cmd.valueOf(line.trim().contains(":") ? line.split(":")[0] : line), line.trim().contains(":") ? line.split(":")[1] : "");
            }
        }

        @Override
        public void onClosedStatus(boolean isClosed) {
//            if (isClosed) {
//                isConnected = false;
//                //connect();
//                displayState(ConnectionDisplayState.CONNECTED);
//            } else {
//
//            }
            
            if (isClosed) {
                notifyDisconnected();
                displayState(ConnectionDisplayState.DISCONNECTED);
            } else {
                setIsConnected(true);
                displayState(ConnectionDisplayState.CONNECTED);
            }
        }
    }
    
    /*
     * Synchronized method to set isConnected boolean
     */
    private synchronized void setIsConnected(boolean connected) {
        this.isConnected = connected;
    }
    
    private synchronized void notifyDisconnected() {
        isConnected = false;
        notifyAll();
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
        //gameServiceNumber = i;
        //gameTo = gt;
        gameTypeLabel.setText(String.valueOf(gt));
    }

    protected void CheckServiceOrder() {
        if(ttgame.getGameCompleted()) return;
        
        if (ttgame.getPlayers().get(TTGame.PlayerSide.LEFT).getServiceOrder()) {
            ChangeServiceColor(ACTIVE_SERVICE, INACTIVE_SERVICE);
        } else {
            ChangeServiceColor(INACTIVE_SERVICE, ACTIVE_SERVICE);
        }
    }

    private void ChangeServiceColor(String l, String r) {
        serviceFlagLeft.getStyleClass().remove(0);
        serviceFlagRight.getStyleClass().remove(0);

        serviceFlagLeft.getStyleClass().add(l);
        serviceFlagRight.getStyleClass().add(r);
    }

    private void ResetBoard(boolean resetScoreCounter) {
        if (resetScoreCounter) {
            ttgame.nextSet();
            ttgame.setGameCompleted(false);
        } else {
            ttgame = new TTGame();
        }
    }

    private void SwitchSides() {
        ttgame.SwitchPlayersSide();
        UpdateUI();
//        String oldSideLeft;
//
//        oldSideLeft = playerNameLeft.getText();
//        playerNameLeft.setText(playerNameRight.getText());
//        playerNameRight.setText(oldSideLeft);
//
//        oldSideLeft = gameCounterLeft.getText();
//        gameCounterLeft.setText(gameCounterRight.getText());
//        gameCounterRight.setText(oldSideLeft);
//
//        oldSideLeft = counterLeft.getText();
//        counterLeft.setText(counterRight.getText());
//        counterRight.setText(oldSideLeft);
    }

    private void ProcessRemoteCmd(Cmd data, String attr) {
        switch (data) {
            case LUP:
                ttgame.UpdatePlayersScore(TableTennisGame.PlayerSide.LEFT);
                CheckServiceOrder();
                break;
            case LDOWN:
                ttgame.DecriseScore(TableTennisGame.PlayerSide.LEFT);
                CheckServiceOrder();
                break;
            case RUP:
                ttgame.UpdatePlayersScore(TableTennisGame.PlayerSide.RIGHT);
                CheckServiceOrder();
                break;
            case RDOWN:
                ttgame.DecriseScore(TableTennisGame.PlayerSide.RIGHT);
                CheckServiceOrder();
                break;
            case RESET:
                ResetBoard(false);
                break;
            case SWITCH:
                SwitchSides();
                break;
            case SCHANGE:
                CheckServiceOrder();
                break;
            case NEXT:
                ttgame.nextSet();
                break;
            case GAME11:
                ttgame.setGameType(TableTennisGame.GameType.G11);
                break;
            case GAME21:
                ttgame.setGameType(TableTennisGame.GameType.G21);
                break;
            case PLLEFT:
                ttgame.getPlayers().get(TTGame.PlayerSide.LEFT).updatePlayerName(attr);
                break;
            case PLRIGHT:
                ttgame.getPlayers().get(TTGame.PlayerSide.RIGHT).updatePlayerName(attr);
                break;
            default:
                throw new AssertionError(data.name());
        }

        UpdateUI();
    }

    private void ProcessEvents(KeyEvent event) {
        if (keycombIncreaseLeft.match(event)) {

        }

        if (keycombDecriseLeft.match(event)) {

        }

        if (keycombIncreaseRight.match(event)) {

        }

        if (keycombDecriseRight.match(event)) {

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

        if (keycombServiceChange.match(event)) {
            CheckServiceOrder();
        }
    }

    private void SetAppKeyHandler() {
        eventHandler = (KeyEvent event) -> {
            //System.out.println(event.getCode());
            ProcessEvents(event);
        };
    }

    private void UpdateUI() {
        UpdatePlayerScore(TTGame.PlayerSide.LEFT, ttgame.getPlayers().get(TTGame.PlayerSide.LEFT).getScoreCounter());
        UpdatePlayerScore(TTGame.PlayerSide.RIGHT, ttgame.getPlayers().get(TTGame.PlayerSide.RIGHT).getScoreCounter());
        UpdateGameScore(TTGame.PlayerSide.LEFT, ttgame.getPlayers().get(TTGame.PlayerSide.LEFT).getGameScore());
        UpdateGameScore(TTGame.PlayerSide.RIGHT, ttgame.getPlayers().get(TTGame.PlayerSide.RIGHT).getGameScore());
        UpdatePlayerName(TTGame.PlayerSide.LEFT, ttgame.getPlayers().get(TTGame.PlayerSide.LEFT).getPlayerName());
        UpdatePlayerName(TTGame.PlayerSide.RIGHT, ttgame.getPlayers().get(TTGame.PlayerSide.RIGHT).getPlayerName());
        UpdateGameType(ttgame.getGameType());
        
        if (socket != null) {
            socket.sendMessage("LP:" + counterLeft.getText() + ";RP:" + counterRight.getText());
        }
    }

}
