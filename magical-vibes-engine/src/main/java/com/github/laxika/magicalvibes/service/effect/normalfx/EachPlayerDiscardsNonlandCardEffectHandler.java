package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsNonlandCardState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsNonlandCardEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

/** Resolves an APNAP discard of one nonland card from each player's hand. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsNonlandCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsNonlandCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsNonlandCardState state = gameData.eachPlayerDiscardsNonlandCard;
        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.currentPlayerId = null;
            state.remaining.clear();
            addInApnapOrder(gameData, state.remaining);
        } else if (state.currentPlayerId != null) {
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            List<Integer> validIndices = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                if (!hand.get(i).hasType(CardType.LAND)) {
                    validIndices.add(i);
                }
            }
            if (validIndices.isEmpty()) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1,
                    validIndices);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void addInApnapOrder(GameData gameData, java.util.Deque<UUID> order) {
        if (gameData.playerIds.contains(gameData.activePlayerId)) {
            order.addLast(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.addLast(playerId);
            }
        }
    }
}
