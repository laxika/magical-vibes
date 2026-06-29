package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.JoinGame;
import com.github.laxika.magicalvibes.networking.message.LobbyGame;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameSetupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application-layer lobby: produces the lobby/game wire views and delegates the actual game
 * bootstrap (seating players, opening sequence) to the engine's {@link GameSetupService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {

    public record GameResult(JoinGame joinGame, LobbyGame lobbyGame) {}

    private final GameRegistry gameRegistry;
    private final GameBroadcastService gameBroadcastService;
    private final GameSetupService gameSetupService;

    public GameResult createGame(String gameName, Player player, String deckId) {
        GameData gameData = gameSetupService.createGame(gameName, player, deckId);
        return new GameResult(gameBroadcastService.getJoinGame(gameData, null), toLobbyGame(gameData));
    }

    public List<LobbyGame> listRunningGames() {
        return gameRegistry.getRunningGames().stream()
                .map(this::toLobbyGame)
                .toList();
    }

    public LobbyGame joinGame(GameData gameData, Player player, String deckId) {
        gameSetupService.joinGame(gameData, player, deckId);
        return toLobbyGame(gameData);
    }

    private LobbyGame toLobbyGame(GameData data) {
        return new LobbyGame(
                data.id,
                data.gameName,
                data.createdByUsername,
                data.playerIds.size(),
                data.status
        );
    }
}
