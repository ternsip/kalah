package com.kalah.rest;

import com.kalah.general.LobbyState;
import com.kalah.general.Player;
import com.kalah.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController("lobby")
@RequiredArgsConstructor
public class LobbyController {

	private final LobbyService lobbyService;

	@GetMapping("/get-random-uuid")
	public UUID getRandomUUID() {
		return UUID.randomUUID();
	}

	@PostMapping("/lobby-reset")
	public LobbyState lobbyReset(
			@RequestParam(name = "initialNumberOfStones") Integer initialNumberOfStones
	) {
		lobbyService.resetLobbyState(initialNumberOfStones);
		return lobbyService.getLobbyState();
	}

	@GetMapping("/lobby-state")
	public LobbyState getLobbyState() {
		return lobbyService.getLobbyState();
	}

	@PostMapping("/player-move")
	public LobbyState playerMove(
			@RequestParam(name = "playerUUID") UUID playerUUID,
			@RequestParam(name = "pitIndex") Integer pitIndex

	) {
		lobbyService.makeMove(playerUUID, pitIndex);
		return lobbyService.getLobbyState();
	}

	@PostMapping("/assign-player")
	public LobbyState assignPlayer(
			@RequestParam(name = "playerUUID") UUID playerUUID,
			@RequestParam(name = "playerName") String playerName
	) {
		lobbyService.assignPlayer(new Player(playerUUID, playerName));
		return lobbyService.getLobbyState();
	}

}