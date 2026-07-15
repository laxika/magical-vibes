package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawUpToNGainLifePerCardBelowEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link EachPlayerMayDrawUpToNGainLifePerCardBelowEffect}: each player, in APNAP order
 * (active player first), chooses how many cards to draw (0 to {@code maxDraw}), draws that many, then
 * gains {@code lifePerCardBelow} life for each card fewer than {@code maxDraw} they drew.
 * <p>
 * The per-player choices are sequential {@code XValueChoice} interactions. The APNAP remainder rides
 * {@link GameData#pendingEachPlayerDrawUpToQueue} (head = currently prompted player); on each re-entry
 * (after the head player answers) we apply their result, pop them, and prompt the next player. Used by
 * Temporary Truce.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDrawUpToNGainLifePerCardBelowEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
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

        if (gameData.chosenXValue != null) {
            // Re-entry: the head player just chose how many cards to draw.
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.remove(0);
            applyDrawAndLife(gameData, entry, e, playerId, chosen);
            promptNextOrFinish(gameData, entry, e);
            return;
        }

        // First entry: build the APNAP-ordered queue (active player first).
        gameData.pendingEachPlayerDrawUpToQueue.clear();
        UUID activePlayerId = gameData.activePlayerId;
        gameData.pendingEachPlayerDrawUpToQueue.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerDrawUpToQueue.add(playerId);
            }
        }
        promptNextOrFinish(gameData, entry, e);
    }

    private void promptNextOrFinish(GameData gameData, StackEntry entry,
                                    EachPlayerMayDrawUpToNGainLifePerCardBelowEffect e) {
        if (gameData.pendingEachPlayerDrawUpToQueue.isEmpty()) {
            return;
        }
        UUID playerId = gameData.pendingEachPlayerDrawUpToQueue.get(0);
        String cardName = entry.getCard().getName();
        String prompt = "Draw up to " + e.maxDraw() + " cards for " + cardName + ". You gain "
                + e.lifePerCardBelow() + " life for each card fewer than " + e.maxDraw() + " you draw.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, e.maxDraw(), prompt, cardName));
    }

    private void applyDrawAndLife(GameData gameData, StackEntry entry,
                                  EachPlayerMayDrawUpToNGainLifePerCardBelowEffect e,
                                  UUID playerId, int chosen) {
        String playerName = gameData.playerIdToName.get(playerId);
        if (chosen > 0) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " draws " + chosen + " card" + (chosen != 1 ? "s" : "") + "."));
        }
        int lifeGained = e.lifePerCardBelow() * (e.maxDraw() - chosen);
        if (lifeGained > 0) {
            lifeSupport.applyGainLife(gameData, playerId, lifeGained, null,
                    entry.getCard(), entry.getEntryType());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " gains " + lifeGained + " life."));
        }
    }
}
