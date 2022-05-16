package com.kalah.general;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PlayerMove {

    private Player player;
    private PlayerTurn playerTurn;
    private int pitPosition;
    private UUID moveUUID; // unique id of the move to distinguish

}
