/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.my.games;

import java.util.HashMap;

/**
 *
 * @author ashmidt
 */
public class TTGame extends TableTennisGame {
    private HashMap<TableTennisGame.PlayerSide, TTPlayer> players = new HashMap<>();
    private int gameTo = 11;
    
    public TTGame() {
        players.put(TableTennisGame.PlayerSide.LEFT, new TTPlayer());
        players.put(TableTennisGame.PlayerSide.RIGHT, new TTPlayer());

        InitializePlayers();
    }

    public HashMap<TableTennisGame.PlayerSide, TTPlayer> getPlayers() {
        return players;
    }

    public void SwitchPlayersSide(){
        TTPlayer p1 = players.get(PlayerSide.LEFT);
        TTPlayer p2 = players.get(PlayerSide.RIGHT);
        
        players.replace(PlayerSide.LEFT, p1, p2);
        players.replace(PlayerSide.RIGHT, p2, p1);
    }
    
    @Override
    public void nextSet() {
        players.forEach((k, v) -> {
            v.setScoreCounter(0);
        });
        setGameCompleted(false);
    }

    private void InitializePlayers() {
        players.forEach((k, v) -> {
            v.setPlayerName("Player " + (k.ordinal() + 1));
            v.setGameScore(0);
            v.setScoreCounter(0);
        });

        setGameType(TableTennisGame.GameType.G11);
        setGameCompleted(false);
        players.get(PlayerSide.LEFT).setServiceOrder(true);
        players.get(PlayerSide.RIGHT).setServiceOrder(false);
    }
    
    private void UpdateServiceOrder(){
        int scoreP1 = players.get(PlayerSide.LEFT).getScoreCounter();
        int scoreP2 = players.get(PlayerSide.RIGHT).getScoreCounter();
        int mod = 0;
        
        switch(getGameTypeEnum()){
            case G11:
                mod = 2;
                break;
            case G21:
                mod = 5;
                break;
            default:
                throw new AssertionError(GameType.valueOf(String.valueOf(getGameType())).name());
        }

        if (((scoreP1 + scoreP2) % mod) == 0) {
            if(players.get(PlayerSide.LEFT).getServiceOrder()){
                players.get(PlayerSide.LEFT).setServiceOrder(false);
                players.get(PlayerSide.RIGHT).setServiceOrder(true);
            }else{
                players.get(PlayerSide.LEFT).setServiceOrder(true);
                players.get(PlayerSide.RIGHT).setServiceOrder(false);
            }
        }
    }
    
    public void UpdatePlayersScore(PlayerSide side){
        int scoreP1 = 0, scoreP2 = 0, counter;
        scoreP1 = players.get(PlayerSide.LEFT).getScoreCounter();
        scoreP2 = players.get(PlayerSide.RIGHT).getScoreCounter();
        int gameTo = getGameType();
        
        if(getGameCompleted()) return;
        
        switch(side){
            case LEFT:
                counter = ++scoreP1;
                break;
            case RIGHT:
                counter = ++scoreP2;
                break;
            default:
                throw new AssertionError(side.name());
            
        }

        if(counter < gameTo){
            players.get(side).setScoreCounter(counter);
        }else{
            if((scoreP1 - scoreP2) == 0){
                System.out.println("Score tie.");
                players.get(side).setScoreCounter(counter);
            }else if(Math.abs((scoreP1 - scoreP2)) > 1){
                players.get(side).setScoreCounter(counter);
                players.get(side).setGameScore(players.get(side).getGameScore() + 1);
                setGameCompleted(true);
                System.out.println("Game completed. Player " + players.get(side).getPlayerName() + " winner." + " (" + scoreP1 + ":" + scoreP2 + ")");
            }else{
                if(!getGameCompleted()){
                    players.get(side).setScoreCounter(counter);
                } else {
                    players.get(side).setGameScore(players.get(side).getGameScore() + 1);
                    System.out.println("Here is the end.");
                }
            }
        }
        
        if(!getGameCompleted()){
            UpdateServiceOrder();
        }
    }

    public void DecriseScore(PlayerSide side){
        if(players.get(side).getScoreCounter() > 0){
            players.get(side).setScoreCounter(players.get(side).getScoreCounter() - 1);
            UpdateServiceOrder();
        }
    }
    
    public class TTPlayer extends TableTennisGame.Player {
        private boolean isMyService = false;
        
        public void updatePlayerName(String playerName) {
            setPlayerName(playerName);
        }
        
        public void setServiceOrder(boolean isMyService){
            this.isMyService = isMyService;
        }
        
        public boolean getServiceOrder(){
            return this.isMyService;
        }
    }
}
