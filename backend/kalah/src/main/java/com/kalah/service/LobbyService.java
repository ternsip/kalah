package com.kalah.service;

import com.kalah.general.LobbyState;
import com.kalah.general.Player;
import com.kalah.general.PlayerMove;
import com.kalah.rest.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

import static com.kalah.general.PlayerMove.PLAYER_A;
import static com.kalah.general.PlayerMove.PLAYER_B;

@Service
public class LobbyService {

    public static final int PIT_LINE_SIZE = 6;
    public static final int PITS_TOTAL = PIT_LINE_SIZE * 2 + 2;
    public static final int PIT_SCORE_A_POS = PIT_LINE_SIZE;
    public static final int PIT_SCORE_B_POS = PIT_LINE_SIZE * 2 + 1;
    public static final int PIT_INITIAL_STONES = 6; // 6, 5, 4, or 3

    private final LobbyState lobbyState = new LobbyState();

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public void resetLobbyState() {
        lobbyState.reset();
    }

    public void assignPlayer(Player player) {
        if (lobbyState.getPlayerA() != null && lobbyState.getPlayerB() != null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Can not assign player. The lobby is full.");
        }
        if (lobbyState.getPlayerA() == null) {
            lobbyState.setPlayerA(player);
        } else {
            lobbyState.setPlayerB(player);
        }
    }

    public void makeMove(UUID playerUUID, int sourcePitIndex) {
        if (lobbyState.getPlayerA() == null || lobbyState.getPlayerB() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Game cannot start until all players connect");
        }
        PlayerMove playerMove = lobbyState.getPlayerMove();
        if (playerMove == PLAYER_A && !lobbyState.getPlayerA().getUuid().equals(playerUUID)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "This player can not move stones now");
        }
        if (playerMove == PLAYER_B && !lobbyState.getPlayerB().getUuid().equals(playerUUID)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "This player can not move stones now");
        }
        if (playerMove == PLAYER_A && (sourcePitIndex < 0 || sourcePitIndex >= PIT_LINE_SIZE)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Player A can move stones only on his line");
        }
        if (playerMove == PLAYER_B && (sourcePitIndex < PIT_LINE_SIZE + 1 || sourcePitIndex >= 2 * PIT_LINE_SIZE + 1)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Player B can move stones only on his line");
        }
        int[] pits = lobbyState.getPits();
        if (pits[sourcePitIndex] == 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Player can not move stones from an empty pit");
        }
        int currentPitIndex = sourcePitIndex;
        int stones = pits[currentPitIndex];
        pits[currentPitIndex] = 0;
        while (stones > 0) {
            currentPitIndex = (currentPitIndex + 1) % pits.length;
            if (currentPitIndex == PIT_SCORE_A_POS && playerMove == PLAYER_B) {
                continue; // SKIP OPPONENT SCORE PIT
            }
            if (currentPitIndex == PIT_SCORE_B_POS && playerMove == PLAYER_A) {
                continue; // SKIP OPPONENT SCORE PIT
            }
            pits[currentPitIndex]++;
            stones--;
        }
        boolean playerAFinishedOnSideA = playerMove == PLAYER_A && currentPitIndex < PIT_SCORE_A_POS;
        boolean playerBFinishedOnSideB = playerMove == PLAYER_B && currentPitIndex > PIT_SCORE_A_POS && currentPitIndex < PIT_SCORE_B_POS;
        boolean playerAScored = playerMove == PLAYER_A && currentPitIndex == PIT_SCORE_A_POS;
        boolean playerBScored = playerMove == PLAYER_B && currentPitIndex == PIT_SCORE_B_POS;
        int oppositePitIndex = getOppositePitIndex(currentPitIndex);
        int oppositePitStones = pits[oppositePitIndex];
        if (pits[currentPitIndex] == 1 && playerAFinishedOnSideA && oppositePitStones > 0) {
            // PLAYER_A END UP ON PLAYER_A SIDE WITH EMPTY PIT AND OPPOSITE IS NOT EMPTY
            pits[PIT_SCORE_A_POS] += oppositePitStones + 1;
            pits[currentPitIndex] = 0;
            pits[oppositePitIndex] = 0;
        }
        if (pits[currentPitIndex] == 1 && playerBFinishedOnSideB && oppositePitStones > 0) {
            // PLAYER_B END UP ON PLAYER_B SIDE WITH EMPTY PIT AND OPPOSITE IS NOT EMPTY
            pits[PIT_SCORE_B_POS] += oppositePitStones + 1;
            pits[currentPitIndex] = 0;
            pits[oppositePitIndex] = 0;
        }
        if (!playerAScored && !playerBScored) {
            // change the turn only if there is no condition for double-move
            lobbyState.setPlayerMove(lobbyState.getPlayerMove().getNextPlayerMove());
        }
        // CHECK GAME ENDING
        int sumA = 0;
        int sumB = 0;
        for (int i = 0; i < PIT_LINE_SIZE; ++i) {
            sumA += pits[i];
            sumB += pits[i + PIT_SCORE_A_POS + 1];
        }
        if (sumA == 0) {
            pits[PIT_SCORE_B_POS] += sumB;
            Arrays.fill(lobbyState.getPits(), PIT_SCORE_A_POS + 1, PIT_SCORE_B_POS, 0);
            lobbyState.setGameActive(false);
        }
        if (sumB == 0) {
            pits[PIT_SCORE_A_POS] += sumA;
            Arrays.fill(lobbyState.getPits(), 0, PIT_SCORE_A_POS, 0);
            lobbyState.setGameActive(false);
        }
    }

    private int getOppositePitIndex(int pitIndex) {
        return (pitIndex + PIT_LINE_SIZE + 1) % PITS_TOTAL;
    }

}
