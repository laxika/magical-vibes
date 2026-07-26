package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.TournamentResultHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Applies post-result runtime cleanup after the terminal result has been projected.
 *
 * <p>All work here runs after the mutation monitor is released. This keeps tournament
 * progression, timer cancellation, AI shutdown, and registry removal out of rules mutation.
 */
@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class GameEndLifecycleSubscriber implements GameEventSubscriber {

    private static final String AI_CONNECTION_PREFIX = "ai-";

    private final GameRegistry gameRegistry;
    private final DraftRegistry draftRegistry;
    private final ObjectProvider<TournamentResultHandler> tournamentResultHandler;
    private final ObjectProvider<GameTimeoutService> gameTimeoutService;
    private final SessionManager sessionManager;

    @Override
    public void onGameEvents(GameEventBatch batch) {
        GameEventFact.GameEnded ended = batch.events().stream()
                .map(envelope -> envelope.fact())
                .filter(GameEventFact.GameEnded.class::isInstance)
                .map(GameEventFact.GameEnded.class::cast)
                .findFirst()
                .orElse(null);
        if (ended == null) {
            return;
        }

        GameData gameData = gameRegistry.get(batch.gameId());
        if (gameData == null) {
            return;
        }
        if (Thread.holdsLock(gameData)) {
            throw new IllegalStateException("Game-end cleanup must run outside the game monitor");
        }

        closeAiConnections(gameData);

        if (ended.result() == GameEventFact.GameResult.WIN && gameData.draftId != null) {
            DraftData draftData = draftRegistry.get(gameData.draftId);
            TournamentResultHandler handler = tournamentResultHandler.getIfAvailable();
            if (draftData != null && handler != null) {
                handler.handleGameFinished(draftData, ended.winnerPlayerId());
            }
        }

        GameTimeoutService timeoutService = gameTimeoutService.getIfAvailable();
        if (timeoutService != null) {
            timeoutService.onGameFinished(gameData);
        }
        gameRegistry.remove(gameData.id);
    }

    private void closeAiConnections(GameData gameData) {
        Set<String> closedConnectionIds = new HashSet<>();
        for (var playerId : gameData.orderedPlayerIds) {
            Connection connection = sessionManager.getConnectionByUserId(playerId);
            if (connection == null
                    || !connection.getId().startsWith(AI_CONNECTION_PREFIX)
                    || !closedConnectionIds.add(connection.getId())) {
                continue;
            }
            try {
                connection.close();
            } catch (Exception failure) {
                log.warn("Failed to close AI connection {} for game {}",
                        connection.getId(), gameData.id, failure);
            } finally {
                sessionManager.unregisterSession(connection.getId());
            }
        }
    }
}
