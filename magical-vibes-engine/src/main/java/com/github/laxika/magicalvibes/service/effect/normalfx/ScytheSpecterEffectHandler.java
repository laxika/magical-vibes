package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ScytheSpecterState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScytheSpecterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Scythe Specter's APNAP discard and greatest-mana-value comparison. */
@Component
@RequiredArgsConstructor
public class ScytheSpecterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScytheSpecterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ScytheSpecterState state = gameData.scytheSpecter;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            addOpponentsInApnapOrder(gameData, state);
        } else if (state.currentPlayerId != null) {
            state.discardedManaValues.put(state.currentPlayerId,
                    gameData.lastDiscardedCardManaValue);
            state.currentPlayerId = null;
        }

        processNextOpponent(gameData, entry, state);
    }

    private void processNextOpponent(GameData gameData, StackEntry entry,
            ScytheSpecterState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()
                    || gameQueryService.isDiscardPrevented(gameData, playerId)) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.lastDiscardedCardManaValue = 0;
            gameData.discardCausedByOpponent = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            state.discardedManaValues.put(playerId, gameData.lastDiscardedCardManaValue);
            state.currentPlayerId = null;
        }

        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, ScytheSpecterState state) {
        int greatestManaValue = state.discardedManaValues.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        List<UUID> lifeLossPlayers = new ArrayList<>();
        if (greatestManaValue > 0) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                Integer manaValue = state.discardedManaValues.get(playerId);
                if (manaValue != null && manaValue == greatestManaValue) {
                    lifeLossPlayers.add(playerId);
                }
            }
        }

        state.reset();
        gameData.discardCausedByOpponent = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
        for (UUID playerId : lifeLossPlayers) {
            lifeSupport.applyLifeLoss(gameData, playerId, greatestManaValue, entry.getCard().getName());
        }
    }

    private void addOpponentsInApnapOrder(GameData gameData, ScytheSpecterState state) {
        List<UUID> orderedPlayers = List.copyOf(gameData.orderedPlayerIds);
        int activePlayerIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex < 0) {
            activePlayerIndex = 0;
        }
        for (int offset = 0; offset < orderedPlayers.size(); offset++) {
            UUID playerId = orderedPlayers.get((activePlayerIndex + offset) % orderedPlayers.size());
            if (!playerId.equals(state.controllerId)) {
                state.remaining.addLast(playerId);
            }
        }
    }
}
