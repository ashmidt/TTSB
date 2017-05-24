/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.my.games;

/**
 *
 * @author ashmidt
 */
public abstract class TableTennisGame {
    public enum PlayerSide {LEFT, RIGHT}
    public enum GameType {
        G11(11), G21(21);
        private final int value;
        
        private GameType(int value){
         this.value = value;   
        }
    }
    
    private int maxGameScore;
    private boolean isGameDone;
    
    public void nextSet(){}
    
    public void setGameType(GameType gt){
        this.maxGameScore = gt.value;
    }
    
    public int getGameType(){
        return this.maxGameScore;
    }
    
    public GameType getGameTypeEnum(){
        return this.maxGameScore == GameType.G11.value ? GameType.G11 : GameType.G21;
    }
    
    public void setGameCompleted(boolean gameCompleted){
        this.isGameDone = gameCompleted;
    }
    
    public boolean getGameCompleted(){
        return this.isGameDone;
    }
    
    public abstract class Player { 
        private String playerName;
        private int playerScore, playerGameScore;
        
        protected void setPlayerName(String playerName){
            this.playerName = playerName;
        }
        
        public String getPlayerName(){return this.playerName;}
        
        protected void setScoreCounter(int score){
            this.playerScore = score;
        }
        
        public int getScoreCounter(){return this.playerScore;}
        
        protected void setGameScore(int score){
            this.playerGameScore = score;
        }
        
        public int getGameScore(){return this.playerGameScore;}
    }
}
