package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOneThenControllerDrawsIfDiscardedState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an each-player discard followed by the controller's conditional draw. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsOneThenControllerDrawsIfDiscardedState state =
                gameData.eachPlayerDiscardsOneThenControllerDrawsIfDiscarded;

        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
                state.remaining.add(gameData.activePlayerId);
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.add(playerId);
                }
            }
        } else if (state.currentPlayerId != null) {
            recordControllerDiscard(gameData, state);
            state.currentPlayerId = null;
        }

        beginNextDiscard(gameData, state);

        if (!state.active) {
            boolean controllerDiscarded = state.controllerDiscarded;
            UUID controllerId = state.controllerId;
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            if (controllerDiscarded) {
                playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
            }
        }
    }

    private void beginNextDiscard(GameData gameData,
            EachPlayerDiscardsOneThenControllerDrawsIfDiscardedState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty()) {
                continue;
            }

            state.currentPlayerId = playerId;
            if (playerId.equals(state.controllerId)) {
                state.controllerDiscardCountBefore =
                        gameData.cardsDiscardedThisTurn.getOrDefault(playerId, 0);
            }
            gameData.discardCausedByOpponent = !playerId.equals(state.controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            recordControllerDiscard(gameData, state);
            state.currentPlayerId = null;
        }

        state.active = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void recordControllerDiscard(GameData gameData,
            EachPlayerDiscardsOneThenControllerDrawsIfDiscardedState state) {
        if (state.currentPlayerId != null && state.currentPlayerId.equals(state.controllerId)
                && gameData.cardsDiscardedThisTurn.getOrDefault(state.controllerId, 0)
                > state.controllerDiscardCountBefore) {
            state.controllerDiscarded = true;
        }
    }
}
