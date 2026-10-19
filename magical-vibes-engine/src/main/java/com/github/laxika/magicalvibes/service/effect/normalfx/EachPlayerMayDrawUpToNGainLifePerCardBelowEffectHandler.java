package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link EachPlayerMayDrawUpToNGainLifePerCardBelowEffect}. The spell's two sentences are
 * separate steps that involve every player (CR 608.2e): first each player, in APNAP order, chooses how
 * many cards to draw (0 to {@code maxDraw}) and only then do the players draw; afterwards each player
 * gains {@code lifePerCardBelow} life for each card fewer than {@code maxDraw} they drew this way, so a
 * draw that a replacement effect turned into something else does not count.
 * <p>
 * The per-player choices are sequential {@code XValueChoice} interactions. The APNAP remainder rides
 * {@link GameData#pendingEachPlayerDrawUpToQueue} (head = currently prompted player) and the answers pile
 * up in {@link GameData#pendingEachPlayerDrawUpToChoices}; once the last player has chosen, the draw counts
 * are snapshotted in {@link GameData#pendingEachPlayerDrawUpToInitialCounts} and the queue is refilled with
 * the players still to draw. Used by Temporary Truce.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDrawUpToNGainLifePerCardBelowEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDrawUpToNGainLifePerCardBelowEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerMayDrawUpToNGainLifePerCardBelowEffect) effect;

        if (gameData.pendingEachPlayerDrawUpToInitialCounts != null) {
            drawThenGainLife(gameData, entry, e);
            return;
        }

        if (gameData.chosenXValue != null) {
            // Re-entry: the head player just chose how many cards to draw.
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.removeFirst();
            gameData.pendingEachPlayerDrawUpToChoices.put(playerId, chosen);
            if (!gameData.pendingEachPlayerDrawUpToQueue.isEmpty()) {
                promptNext(gameData, entry, e);
                return;
            }
            Map<UUID, Integer> initialCounts = new LinkedHashMap<>();
            gameData.pendingEachPlayerDrawUpToChoices.keySet().forEach(id ->
                    initialCounts.put(id, gameData.cardsDrawnThisTurn.getOrDefault(id, 0)));
            gameData.pendingEachPlayerDrawUpToInitialCounts = initialCounts;
            gameData.pendingEachPlayerDrawUpToQueue.addAll(gameData.pendingEachPlayerDrawUpToChoices.keySet());
            drawThenGainLife(gameData, entry, e);
            return;
        }

        // First entry: build the APNAP-ordered queue (active player first).
        gameData.pendingEachPlayerDrawUpToQueue.clear();
        gameData.pendingEachPlayerDrawUpToChoices.clear();
        UUID activePlayerId = gameData.activePlayerId;
        gameData.pendingEachPlayerDrawUpToQueue.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerDrawUpToQueue.add(playerId);
            }
        }
        promptNext(gameData, entry, e);
    }

    private void promptNext(GameData gameData, StackEntry entry,
                            EachPlayerMayDrawUpToNGainLifePerCardBelowEffect e) {
        UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.get(0);
        String cardName = entry.getCard().getName();
        String prompt = "Draw up to " + e.maxDraw() + " cards for " + cardName + ". You gain "
                + e.lifePerCardBelow() + " life for each card fewer than " + e.maxDraw() + " you draw.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, e.maxDraw(), prompt, cardName));
    }

    /**
     * Every player has chosen: each draws their chosen count in APNAP order (pausing for any prompt a
     * draw opens and resuming here), then each gains life for the cards short of the maximum.
     */
    private void drawThenGainLife(GameData gameData, StackEntry entry,
                                  EachPlayerMayDrawUpToNGainLifePerCardBelowEffect e) {
        while (!gameData.pendingEachPlayerDrawUpToQueue.isEmpty()) {
            UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.removeFirst();
            int chosen = gameData.pendingEachPlayerDrawUpToChoices.getOrDefault(playerId, 0);
            if (chosen > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
            }
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                return;
            }
        }

        Map<UUID, Integer> initialCounts = gameData.pendingEachPlayerDrawUpToInitialCounts;
        gameData.pendingEachPlayerDrawUpToInitialCounts = null;
        List<UUID> players = List.copyOf(gameData.pendingEachPlayerDrawUpToChoices.keySet());
        gameData.pendingEachPlayerDrawUpToChoices.clear();
        for (UUID playerId : players) {
            int drawn = gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0) - initialCounts.get(playerId);
            int lifeGained = e.lifePerCardBelow() * Math.max(0, e.maxDraw() - drawn);
            if (lifeGained > 0) {
                lifeSupport.applyGainLife(gameData, playerId, lifeGained, null,
                        entry.getCard(), entry.getEntryType());
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId) + " gains " + lifeGained + " life."));
            }
        }
    }
}
