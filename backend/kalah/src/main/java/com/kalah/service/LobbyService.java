package com.kalah.service;

import com.kalah.general.LobbyState;
import com.kalah.general.Player;
import com.kalah.general.PlayerMove;
import com.kalah.general.PlayerTurn;
import com.kalah.rest.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

import static com.kalah.general.PlayerTurn.PLAYER_A;
import static com.kalah.general.PlayerTurn.PLAYER_B;

@Service
public class LobbyService {

    public static final int PIT_LINE_SIZE = 6;
    public static final int PITS_TOTAL = PIT_LINE_SIZE * 2 + 2;
    public static final int PIT_SCORE_A_POS = PIT_LINE_SIZE;
    public static final int PIT_SCORE_B_POS = PIT_LINE_SIZE * 2 + 1;

    private final LobbyState lobbyState = new LobbyState();

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public void resetLobbyState(int initialNumberOfStones) {
        if (initialNumberOfStones < 1) {
            // I know that classical rules allow only 3 stones, but I want even one per pit as minimum
            throw new HttpException(HttpStatus.BAD_REQUEST, "Number of stones can not be less than 1");
        }
        if (initialNumberOfStones > 6) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Number of stones can not be more than 6");
        }
        lobbyState.reset(initialNumberOfStones);
    }

    public void assignPlayer(Player player) {
        if (lobbyState.getPlayerA() != null && lobbyState.getPlayerB() != null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Can not assign player. The lobby is full.");
        }
        if (lobbyState.getPlayerA() != null && lobbyState.getPlayerA().getUuid().equals(player.getUuid())) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "This player has already been assigned to the game");
        }
        if (lobbyState.getPlayerA() == null) {
            lobbyState.setPlayerA(player);
        } else {
            lobbyState.setPlayerB(player);
        }
        if (lobbyState.getPlayerA() != null && lobbyState.getPlayerB() != null) {
            lobbyState.setGameStarted(true);
        }
    }

    public void makeMove(UUID playerUUID, int sourcePitIndex) {
        if (!lobbyState.isGameStarted()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Game cannot start until all players connect");
        }
        if (lobbyState.isGameFinished()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Game is finished");
        }
        PlayerTurn playerTurn = lobbyState.getPlayerTurn();
        if (playerTurn == PLAYER_A && !lobbyState.getPlayerA().getUuid().equals(playerUUID)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "This player can not move stones now");
        }
        if (playerTurn == PLAYER_B && !lobbyState.getPlayerB().getUuid().equals(playerUUID)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "This player can not move stones now");
        }
        if (playerTurn == PLAYER_A && (sourcePitIndex < 0 || sourcePitIndex >= PIT_LINE_SIZE)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Player A can move stones only on his line");
        }
        if (playerTurn == PLAYER_B && (sourcePitIndex < PIT_LINE_SIZE + 1 || sourcePitIndex >= 2 * PIT_LINE_SIZE + 1)) {
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
            if (currentPitIndex == PIT_SCORE_A_POS && playerTurn == PLAYER_B) {
                continue; // SKIP OPPONENT SCORE PIT
            }
            if (currentPitIndex == PIT_SCORE_B_POS && playerTurn == PLAYER_A) {
                continue; // SKIP OPPONENT SCORE PIT
            }
            pits[currentPitIndex]++;
            stones--;
        }
        boolean playerAFinishedOnSideA = playerTurn == PLAYER_A && currentPitIndex < PIT_SCORE_A_POS;
        boolean playerBFinishedOnSideB = playerTurn == PLAYER_B && currentPitIndex > PIT_SCORE_A_POS && currentPitIndex < PIT_SCORE_B_POS;
        boolean playerAScored = playerTurn == PLAYER_A && currentPitIndex == PIT_SCORE_A_POS;
        boolean playerBScored = playerTurn == PLAYER_B && currentPitIndex == PIT_SCORE_B_POS;
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
            lobbyState.setPlayerTurn(lobbyState.getPlayerTurn().getNextPlayerMove());
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
            lobbyState.setGameFinished(true);
        }
        if (sumB == 0) {
            pits[PIT_SCORE_A_POS] += sumA;
            Arrays.fill(lobbyState.getPits(), 0, PIT_SCORE_A_POS, 0);
            lobbyState.setGameFinished(true);
        }
        Player player = lobbyState.getPlayerTurn() == PLAYER_A ? lobbyState.getPlayerA() : lobbyState.getPlayerB();
        lobbyState.setPlayerMove(new PlayerMove(player, playerTurn, sourcePitIndex, UUID.randomUUID()));
    }

    private int getOppositePitIndex(int pitIndex) {
        return PIT_SCORE_B_POS - pitIndex - 1;
    }

}
