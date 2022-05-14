package com.kalah.general;

public enum PlayerMove {

    PLAYER_A,
    PLAYER_B;

    public PlayerMove getNextPlayerMove() {
        return this == PLAYER_A ? PLAYER_B : PLAYER_A;
    }

}
