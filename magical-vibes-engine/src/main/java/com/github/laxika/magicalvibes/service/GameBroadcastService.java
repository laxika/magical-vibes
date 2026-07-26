package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.networking.model.GameLogEntryView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Compatibility facade for workflows that have not migrated to domain events yet.
 *
 * <p>View construction and delivery are delegated to the same canonical components used by the
 * event subscriber. Game-log append remains here until the dedicated log migration.
 */
@Component
@RequiredArgsConstructor
public class GameBroadcastService {

    private final GameViewProjectionFactory projectionFactory;
    private final GameMessageTransport transport;
    private final GameLogViewFactory gameLogViewFactory;
    private final GameMutationCoordinator mutationCoordinator;

    public void broadcastGameState(GameData gameData) {
        if (gameData.simulation) {
            return;
        }
        int logSize = gameData.gameLog.size();
        List<GameLogEntryView> newLogEntries = logSize > gameData.lastBroadcastedLogSize
                ? gameLogViewFactory.createAll(
                        gameData.gameLog.subList(gameData.lastBroadcastedLogSize, logSize))
                : List.of();
        gameData.lastBroadcastedLogSize = logSize;
        transport.sendPlayerMessages(projectionFactory.createGameStateMessages(gameData, newLogEntries));
    }

    /**
     * Canonical event-backed replacement for effect-owned state broadcasts.
     */
    public void invalidateAllPlayerViews(GameData gameData) {
        mutationCoordinator.invalidateAllPlayerViews(gameData);
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId) {
        return projectionFactory.getPlayableCardIndices(gameData, playerId);
    }

    public List<Integer> getPlayableCardIndices(GameData gameData, UUID playerId, int extraConvokeMana) {
        return projectionFactory.getPlayableCardIndices(gameData, playerId, extraConvokeMana);
    }

    public List<Integer> getPotentialPlayableCardIndices(
            GameData gameData, UUID playerId, List<Integer> strictIndices) {
        return projectionFactory.getPotentialPlayableCardIndices(gameData, playerId, strictIndices);
    }

    public int getPotentialManaTotal(GameData gameData, UUID playerId) {
        return projectionFactory.getPotentialManaTotal(gameData, playerId);
    }

    public Map<UUID, List<Integer>> getPotentialPayableAbilityIndices(
            GameData gameData, UUID playerId) {
        return projectionFactory.getPotentialPayableAbilityIndices(gameData, playerId);
    }

    public boolean isCardPlayable(
            GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalGenericCost) {
        return projectionFactory.isCardPlayable(
                gameData, playerId, card, pool, additionalGenericCost);
    }

    public List<Integer> getPlayableGraveyardLandIndices(GameData gameData, UUID playerId) {
        return projectionFactory.getPlayableGraveyardLandIndices(gameData, playerId);
    }

    public List<Integer> getPlayableFlashbackIndices(GameData gameData, UUID playerId) {
        return projectionFactory.getPlayableFlashbackIndices(gameData, playerId);
    }

    public void logAndBroadcast(GameData gameData, GameLogEntry logEntry) {
        gameData.gameLog.add(logEntry);
    }

    record FaceDownReveal(UUID viewerId, List<com.github.laxika.magicalvibes.networking.model.CardView> cards) {
    }

    Map<UUID, FaceDownReveal> collectFaceDownReveals(GameData gameData) {
        Map<UUID, GameViewProjectionFactory.FaceDownReveal> projected =
                projectionFactory.collectFaceDownReveals(gameData);
        Map<UUID, FaceDownReveal> result = new java.util.HashMap<>();
        projected.forEach((id, reveal) ->
                result.put(id, new FaceDownReveal(reveal.viewerId(), reveal.cards())));
        return result;
    }

    List<List<PermanentView>> applyFaceDownReveals(
            List<List<PermanentView>> battlefields,
            Map<UUID, FaceDownReveal> reveals,
            UUID viewerId) {
        Map<UUID, GameViewProjectionFactory.FaceDownReveal> projected = new java.util.HashMap<>();
        reveals.forEach((id, reveal) -> projected.put(
                id, new GameViewProjectionFactory.FaceDownReveal(reveal.viewerId(), reveal.cards())));
        return projectionFactory.applyFaceDownReveals(battlefields, projected, viewerId);
    }

    List<List<PermanentView>> getBattlefields(GameData gameData) {
        return projectionFactory.getBattlefields(gameData);
    }
}
