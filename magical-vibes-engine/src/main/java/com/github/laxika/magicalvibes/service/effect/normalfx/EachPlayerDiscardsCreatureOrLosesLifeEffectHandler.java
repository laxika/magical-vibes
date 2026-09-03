package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsCreatureOrLosesLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsCreatureOrLosesLifeEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an APNAP discard flow followed by life loss for noncreature discards. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsCreatureOrLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsCreatureOrLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsCreatureOrLosesLifeEffect discardEffect =
                (EachPlayerDiscardsCreatureOrLosesLifeEffect) effect;
        EachPlayerDiscardsCreatureOrLosesLifeState state = gameData.eachPlayerDiscardsCreatureOrLosesLife;

        if (!state.active) {
            state.active = true;
            state.currentPlayerId = null;
            state.remaining.clear();
            state.playersWhoDiscardedCreature.clear();
            addInApnapOrder(gameData, state.remaining);
        } else if (state.currentPlayerId != null) {
            if (gameData.lastDiscardedCardTypes.contains(CardType.CREATURE)) {
                state.playersWhoDiscardedCreature.add(state.currentPlayerId);
            }
            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        processNextPlayer(gameData, entry, discardEffect, state);
    }

    private void processNextPlayer(GameData gameData, StackEntry entry,
                                   EachPlayerDiscardsCreatureOrLosesLifeEffect effect,
                                   EachPlayerDiscardsCreatureOrLosesLifeState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            state.currentPlayerId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        finish(gameData, entry, effect, state);
    }

    private void finish(GameData gameData, StackEntry entry,
                        EachPlayerDiscardsCreatureOrLosesLifeEffect effect,
                        EachPlayerDiscardsCreatureOrLosesLifeState state) {
        List<UUID> lifeLossPlayers = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!state.playersWhoDiscardedCreature.contains(playerId)) {
                lifeLossPlayers.add(playerId);
            }
        }

        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        for (UUID playerId : lifeLossPlayers) {
            lifeSupport.applyLifeLoss(gameData, playerId, effect.lifeLoss(), entry.getCard().getName());
        }
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
