package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.KroxaDiscardState;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.KroxaDiscardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Kroxa's opponent discard and nonland comparison. */
@Component
@RequiredArgsConstructor
public class KroxaDiscardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return KroxaDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        KroxaDiscardState state = gameData.kroxaDiscard;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            addOpponentsInApnapOrder(gameData, state);
        } else if (state.currentPlayerId != null) {
            state.discardedNonland.put(state.currentPlayerId,
                    !gameData.lastDiscardedCardTypes.isEmpty()
                            && !gameData.lastDiscardedCardTypes.contains(CardType.LAND));
            state.currentPlayerId = null;
        }

        processNextOpponent(gameData, entry, state);
    }

    private void processNextOpponent(GameData gameData, StackEntry entry, KroxaDiscardState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()
                    || gameQueryService.isDiscardPrevented(gameData, playerId)) {
                state.discardedNonland.put(playerId, false);
                state.currentPlayerId = null;
                continue;
            }

            gameData.lastDiscardedCardTypes = java.util.Set.of();
            gameData.discardCausedByOpponent = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            return;
        }

        finish(gameData, entry, state);
    }

    private void finish(GameData gameData, StackEntry entry, KroxaDiscardState state) {
        List<UUID> lifeLossPlayers = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(state.controllerId)
                    && !Boolean.TRUE.equals(state.discardedNonland.get(playerId))) {
                lifeLossPlayers.add(playerId);
            }
        }

        state.reset();
        gameData.discardCausedByOpponent = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
        for (UUID playerId : lifeLossPlayers) {
            lifeSupport.applyLifeLoss(gameData, playerId, 3, entry.getCard().getName());
        }
    }

    private void addOpponentsInApnapOrder(GameData gameData, KroxaDiscardState state) {
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
