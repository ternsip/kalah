package com.kalah.service;

import com.kalah.general.LobbyState;
import com.kalah.general.Player;
import com.kalah.general.PlayerTurn;
import com.kalah.rest.HttpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static com.kalah.service.LobbyService.*;


class LobbyServiceTest {

    @Test
    void assignDoublePlayersOkTest() {
        LobbyService lobbyService = new LobbyService();
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
    }

    @Test
    void assignPlayerTwiceTest() {
        HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
            LobbyService lobbyService = new LobbyService();
            Player playerA = new Player(UUID.randomUUID(), "IVAN");
            lobbyService.assignPlayer(playerA);
            lobbyService.assignPlayer(playerA);
        });
        Assertions.assertEquals(thrown.getMessage(), "This player has already been assigned to the game");
    }

    @Test
    void assignNoOneAndMoveTest() {
        HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
            LobbyService lobbyService = new LobbyService();
            lobbyService.makeMove(UUID.randomUUID(), 4);
        });
        Assertions.assertEquals(thrown.getMessage(), "Game cannot start until all players connect");
    }

    @Test
    void tryToMoveOutsideOfBoundsPlayerATest() {
        LobbyService lobbyService = new LobbyService();
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        for (int i = -100; i < 0; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerA.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "Player A can move stones only on his line");
        }
        for (int i = LobbyService.PIT_SCORE_A_POS; i < 100; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerA.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "Player A can move stones only on his line");
        }
    }

    @Test
    void tryToMoveOutsideOfBoundsPlayerBTest() {
        LobbyService lobbyService = new LobbyService();
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        lobbyService.makeMove(playerA.getUuid(), 0);
        lobbyService.makeMove(playerA.getUuid(), 1);
        for (int i = -100; i <= LobbyService.PIT_SCORE_A_POS; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerB.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "Player B can move stones only on his line");
        }
        for (int i = PIT_SCORE_B_POS; i < 100; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerB.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "Player B can move stones only on his line");
        }
    }

    @Test
    void tryToMoveWrongPlayerTest() {
        LobbyService lobbyService = new LobbyService();
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        for (int i = -100; i < 100; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerB.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "This player can not move stones now");
        }
        lobbyService.makeMove(playerA.getUuid(), 0);
        lobbyService.makeMove(playerA.getUuid(), 1);
        for (int i = -100; i < 100; ++i) {
            int finalI = i;
            HttpException thrown = Assertions.assertThrows(HttpException.class, () -> {
                lobbyService.makeMove(playerA.getUuid(), finalI);
            });
            Assertions.assertEquals(thrown.getMessage(), "This player can not move stones now");
        }
    }

    @Test
    void tryToMoveSomethingTest() {
        LobbyService lobbyService = new LobbyService();
        int stones = 3;
        lobbyService.resetLobbyState(stones);
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        LobbyState lobbyState = lobbyService.getLobbyState();
        Assertions.assertEquals(lobbyState.getPlayerTurn(), PlayerTurn.PLAYER_A);
        lobbyService.makeMove(playerA.getUuid(), PIT_LINE_SIZE - 1);
        lobbyState = lobbyService.getLobbyState();
        Assertions.assertEquals(lobbyState.getPits()[PIT_LINE_SIZE - 1], 0);
        Assertions.assertEquals(lobbyState.getPits()[PIT_LINE_SIZE], 1);
        Assertions.assertEquals(lobbyState.getPits()[PIT_LINE_SIZE + 1], stones + 1);
        Assertions.assertEquals(lobbyState.getPlayerTurn(), PlayerTurn.PLAYER_B);
    }

    @Test
    void tryFinishTest() {
        LobbyService lobbyService = new LobbyService();
        lobbyService.resetLobbyState(1);
        int[] pits = lobbyService.getLobbyState().getPits();
        Arrays.fill(pits, 0);
        pits[PIT_LINE_SIZE - 1] = 1;
        pits[PIT_LINE_SIZE + 1] = 1;
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        LobbyState lobbyState = lobbyService.getLobbyState();
        Assertions.assertTrue(lobbyState.isGameStarted());
        Assertions.assertFalse(lobbyState.isGameFinished());
        lobbyService.makeMove(playerA.getUuid(), PIT_LINE_SIZE - 1);
        Assertions.assertTrue(lobbyState.isGameStarted());
        Assertions.assertTrue(lobbyState.isGameFinished());
        Assertions.assertEquals(pits[PIT_SCORE_A_POS], 1);
        Assertions.assertEquals(pits[PIT_SCORE_B_POS], 1);
        for (int i = 0 ; i < PIT_SCORE_A_POS; ++i) {
            Assertions.assertEquals(pits[i], 0);
        }
        for (int i = PIT_SCORE_A_POS + 1 ; i < PIT_SCORE_B_POS; ++i) {
            Assertions.assertEquals(pits[i], 0);
        }
    }

    @Test
    void stopOnEmptyCellTest() {
        LobbyService lobbyService = new LobbyService();
        lobbyService.resetLobbyState(1);
        int[] pits = lobbyService.getLobbyState().getPits();
        Arrays.fill(pits, 0);
        pits[PIT_LINE_SIZE - 2] = 1;
        pits[PIT_LINE_SIZE + 1] = 3;
        Player playerA = new Player(UUID.randomUUID(), "IVAN");
        Player playerB = new Player(UUID.randomUUID(), "AARON");
        lobbyService.assignPlayer(playerA);
        lobbyService.assignPlayer(playerB);
        LobbyState lobbyState = lobbyService.getLobbyState();
        Assertions.assertTrue(lobbyState.isGameStarted());
        Assertions.assertFalse(lobbyState.isGameFinished());
        lobbyService.makeMove(playerA.getUuid(), PIT_LINE_SIZE - 2);
        Assertions.assertTrue(lobbyState.isGameStarted());
        Assertions.assertTrue(lobbyState.isGameFinished());
        Assertions.assertEquals(pits[PIT_SCORE_A_POS], 4); // SUM UP ALL ROCKS WITH OPPOSITE AND SCORE
        Assertions.assertEquals(pits[PIT_SCORE_B_POS], 0);
    }

}