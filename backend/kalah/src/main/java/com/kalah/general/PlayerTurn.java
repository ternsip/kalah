package com.kalah.general;

public enum PlayerTurn {

    PLAYER_A,
    PLAYER_B;

    public PlayerTurn getNextPlayerMove() {
        return this == PLAYER_A ? PLAYER_B : PLAYER_A;
    }

}
