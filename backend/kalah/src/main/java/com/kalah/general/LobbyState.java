package com.kalah.general;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

import static com.kalah.service.LobbyService.*;

@AllArgsConstructor
@Getter
@Setter
public class LobbyState {

    private int pitsLineSize;
    private int[] pits; // [PITS_A,SCORE_A,PITS_B,SCORE_B] LOOP
    private PlayerMove playerMove;
    private boolean gameActive;
    private Player playerA;
    private Player playerB;

    public LobbyState() {
        this.pits = new int[PITS_TOTAL];
        this.playerMove = PlayerMove.PLAYER_A;
        this.gameActive = true;
        this.playerA = null; // initially not connected
        this.playerB = null; // initially not connected
        reset();
    }

    public void reset() {
        Arrays.fill(pits, PIT_INITIAL_STONES);
        pits[PIT_SCORE_A_POS] = 0;
        pits[PIT_SCORE_B_POS] = 0;
    }

}
