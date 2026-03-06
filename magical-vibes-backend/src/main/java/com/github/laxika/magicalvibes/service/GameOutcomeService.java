package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class GameOutcomeService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final GameRegistry gameRegistry;
    private final DraftRegistry draftRegistry;
    private final DraftService draftService;

    public GameOutcomeService(GameQueryService gameQueryService,
                              GameBroadcastService gameBroadcastService,
                              SessionManager sessionManager,
                              GameRegistry gameRegistry,
                              DraftRegistry draftRegistry,
                              DraftService draftService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.sessionManager = sessionManager;
        this.gameRegistry = gameRegistry;
        this.draftRegistry = draftRegistry;
        this.draftService = draftService;
    }

    public boolean checkWinCondition(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            int life = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            int poison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
            if (life <= 0 || poison >= 10) {
                // Check if the player is protected from losing (e.g. Platinum Angel)
                if (!gameQueryService.canPlayerLoseGame(gameData, playerId)) {
                    continue;
                }

                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                String winnerName = gameData.playerIdToName.get(winnerId);

                gameData.status = GameStatus.FINISHED;

                String logEntry;
                if (poison >= 10) {
                    logEntry = gameData.playerIdToName.get(playerId) + " has 10 poison counters and loses! " + winnerName + " wins!";
                } else {
                    logEntry = gameData.playerIdToName.get(playerId) + " has been defeated! " + winnerName + " wins!";
                }
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

                notifyDraftIfTournamentGame(gameData, winnerId);

                gameRegistry.remove(gameData.id);

                log.info("Game {} - {} wins! {} is at {} life, {} poison", gameData.id, winnerName,
                        gameData.playerIdToName.get(playerId), life, poison);
                return true;
            }
        }
        return false;
    }

    public void declareWinner(GameData gameData, UUID winnerId) {
        String winnerName = gameData.playerIdToName.get(winnerId);

        gameData.status = GameStatus.FINISHED;

        String logEntry = winnerName + " wins the game!";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new GameOverMessage(winnerId, winnerName));

        notifyDraftIfTournamentGame(gameData, winnerId);

        gameRegistry.remove(gameData.id);

        log.info("Game {} - {} wins!", gameData.id, winnerName);
    }

    private void notifyDraftIfTournamentGame(GameData gameData, UUID winnerId) {
        if (gameData.draftId != null) {
            DraftData draftData = draftRegistry.get(gameData.draftId);
            if (draftData != null) {
                draftService.handleGameFinished(draftData, winnerId);
            }
        }
    }
}
