package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameTimeoutService;
import com.github.laxika.magicalvibes.service.TournamentResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Applies post-result runtime cleanup after the terminal result has been projected.
 *
 * <p>All work here runs after the mutation monitor is released. This keeps tournament
 * progression, timer cancellation, and registry removal out of rules mutation. AI schedulers
 * close independently in the AI event subscriber.
 */
@Component
@Order(100)
@RequiredArgsConstructor
public class GameEndLifecycleSubscriber implements GameEventSubscriber {

    private final GameRegistry gameRegistry;
    private final DraftRegistry draftRegistry;
    private final ObjectProvider<TournamentResultHandler> tournamentResultHandler;
    private final ObjectProvider<GameTimeoutService> gameTimeoutService;

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

}
