package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerRummageState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Resolves an each-player discard followed by the controller's conditional draw (Fanatic of the
 * Harrowing). Each player chooses a card in APNAP order (CR 101.4); the chosen cards are discarded
 * simultaneously once every choice is made, and the controller draws one card only if their own
 * chosen card was discarded.
 *
 * <p>Selections are deferred through {@link EachPlayerRummageState}, the same progress state that
 * drives the other deferred each-player discard flows, and the handler re-runs on each completed
 * selection via {@code rerunCurrentEffectAfterInteraction}.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final ObjectProvider<CardChoiceHandlerService> cardChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;

        if (!state.active) {
            state.reset();
            state.active = true;
            state.deferDiscards = true;
            if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
                state.remaining.addLast(gameData.activePlayerId);
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.addLast(playerId);
                }
            }
        }

        beginNextDiscard(gameData, entry);
    }

    private void beginNextDiscard(GameData gameData, StackEntry entry) {
        EachPlayerRummageState state = gameData.eachPlayerRummage;
        UUID controllerId = entry.getControllerId();

        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.pollFirst();
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty() || !gameQueryService.canEffectCauseDiscard(gameData, playerId, controllerId)) {
                continue;
            }

            state.currentPlayerId = playerId;
            gameData.discardCausedByOpponent = !playerId.equals(controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            state.currentPlayerId = null;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        var discardedCounts = cardChoiceHandlerServiceProvider.getObject().discardCollectedCards(
                gameData, List.copyOf(state.selectedDiscards), controllerId);
        state.reset();
        if (discardedCounts.getOrDefault(controllerId, 0) > 0) {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
        }
    }
}
