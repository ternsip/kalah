package com.kalah.general;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.kalah.service.LobbyService.*;

@AllArgsConstructor
@Getter
@Setter
public class LobbyState {

    private int pitsLineSize;
    private int[] pits; // [PITS_A,SCORE_A,PITS_B,SCORE_B] LOOP
    private PlayerTurn playerTurn;
    private PlayerMove playerMove;
    private boolean gameStarted;
    private boolean gameFinished;
    private Player playerA;
    private Player playerB;

    public LobbyState() {
        this.pits = new int[PITS_TOTAL];
        this.playerTurn = PlayerTurn.PLAYER_A;
        this.playerMove = null;
        this.gameStarted = false;
        this.gameFinished = false;
        this.playerA = null; // initially not connected
        this.playerB = null; // initially not connected
        reset(6);
    }

    public void reset(int initialNumberOfStones) {
        Arrays.fill(pits, initialNumberOfStones);
        pits[PIT_SCORE_A_POS] = 0;
        pits[PIT_SCORE_B_POS] = 0;
        playerTurn = PlayerTurn.PLAYER_A;
        playerMove = null;
        gameStarted = false;
        gameFinished = false;
        playerA = null;
        playerB = null;
    }

}
