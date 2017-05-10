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

    @Override
    public void nextSet() {
        players.forEach((k, v) -> {
            v.setScoreCounter(0);
        });
    }

    private void InitializePlayers() {
        players.forEach((k, v) -> {
            v.setPlayerName("Player " + k.ordinal());
            v.setGameScore(0);
            v.setScoreCounter(0);
        });

        setGameType(TableTennisGame.GameType.G11);
        setGameCompleted(false);
    }
    
    public void UpdatePlayersScore(PlayerSide side){
        int scoreP1 = players.get(PlayerSide.LEFT).getScoreCounter();
        int scoreP2 = players.get(PlayerSide.RIGHT).getScoreCounter();
        int counter = 0;
        
        if(++counter < getGameType()){
            players.get(side).setScoreCounter(counter);
        }else{
            if((scoreP1 - scoreP2) == 0){
                players.get(side).setScoreCounter(counter);
            }else if(Math.abs((scoreP1 - scoreP2)) > 1){
                players.get(side).setScoreCounter(counter);
                setGameCompleted(true);
            }else{
                if(!getGameCompleted()){
                    players.get(side).setScoreCounter(counter);
                }
            }
        }
    }

    public void DecriseScore(PlayerSide side){
        if((players.get(side).getScoreCounter() + 1) > 0){
            players.get(side).setScoreCounter(-1);
        }
    }
    
    public class TTPlayer extends TableTennisGame.Player {

        public void updatePlayerName(String playerName) {
            setPlayerName(playerName);
        }
    }
}
