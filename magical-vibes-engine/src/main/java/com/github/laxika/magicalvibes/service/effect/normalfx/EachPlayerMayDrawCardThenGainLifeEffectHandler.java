package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawCardThenGainLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Kwain's independent draw choices, then rewards the players who actually drew. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayDrawCardThenGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayDrawCardThenGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.pendingEachPlayerMayDrawThenGainLifeInitialCount != null) {
            finishCurrentPlayer(gameData, entry);
            return;
        }

        if (gameData.chosenXValue != null) {
            int chosen = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID playerId = gameData.pendingEachPlayerMayDrawThenGainLifeQueue.getFirst();
            gameData.pendingEachPlayerMayDrawThenGainLifeInitialCount =
                    gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
            if (chosen > 0) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, chosen);
            }
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                return;
            }
            finishCurrentPlayer(gameData, entry);
            return;
        }

        gameData.pendingEachPlayerMayDrawThenGainLifeQueue.clear();
        gameData.pendingEachPlayerMayDrawThenGainLifeDrawers.clear();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            gameData.pendingEachPlayerMayDrawThenGainLifeQueue.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                gameData.pendingEachPlayerMayDrawThenGainLifeQueue.add(playerId);
            }
        }
        promptNextPlayer(gameData, entry);
    }

    private void promptNextPlayer(GameData gameData, StackEntry entry) {
        if (gameData.pendingEachPlayerMayDrawThenGainLifeQueue.isEmpty()) {
            return;
        }
        String cardName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                gameData.pendingEachPlayerMayDrawThenGainLifeQueue.getFirst(), 1,
                "Draw a card for " + cardName + "?", cardName));
    }

    private void finishCurrentPlayer(GameData gameData, StackEntry entry) {
        UUID playerId = gameData.pendingEachPlayerMayDrawThenGainLifeQueue.removeFirst();
        boolean drew = gameData.pendingEachPlayerMayDrawThenGainLifeInitialCount != null
                && gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0)
                > gameData.pendingEachPlayerMayDrawThenGainLifeInitialCount;
        gameData.pendingEachPlayerMayDrawThenGainLifeInitialCount = null;
        if (drew) {
            gameData.pendingEachPlayerMayDrawThenGainLifeDrawers.add(playerId);
        }

        if (!gameData.pendingEachPlayerMayDrawThenGainLifeQueue.isEmpty()) {
            promptNextPlayer(gameData, entry);
            return;
        }

        for (UUID drawerId : gameData.pendingEachPlayerMayDrawThenGainLifeDrawers) {
            lifeSupport.applyGainLife(gameData, drawerId, 1, null,
                    entry.getCard(), entry.getEntryType());
        }
        gameData.pendingEachPlayerMayDrawThenGainLifeDrawers.clear();
    }
}
