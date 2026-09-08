package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOrLosesLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOrLosesLifeEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the APNAP discard-or-life-loss flow used by Liliana, Waker of the Dead. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOrLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOrLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsOrLosesLifeEffect discardEffect = (EachPlayerDiscardsOrLosesLifeEffect) effect;
        EachPlayerDiscardsOrLosesLifeState state = gameData.eachPlayerDiscardsOrLosesLife;

        if (!state.active) {
            state.active = true;
            state.currentPlayerId = null;
            state.discardPending = false;
            state.remaining.clear();
            addInApnapOrder(gameData, state.remaining);
        } else if (state.discardPending) {
            state.discardPending = false;
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        processNextPlayer(gameData, entry, discardEffect, state);
    }

    private void processNextPlayer(GameData gameData, StackEntry entry,
                                   EachPlayerDiscardsOrLosesLifeEffect effect,
                                   EachPlayerDiscardsOrLosesLifeState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                if (!playerId.equals(entry.getControllerId())) {
                    lifeSupport.applyLifeLoss(gameData, playerId, effect.lifeLoss(), entry.getCard().getName());
                }
                continue;
            }

            state.discardPending = true;
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            return;
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private void addInApnapOrder(GameData gameData, java.util.Deque<UUID> order) {
        if (gameData.orderedPlayerIds.contains(gameData.activePlayerId)) {
            order.addLast(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.addLast(playerId);
            }
        }
    }
}
