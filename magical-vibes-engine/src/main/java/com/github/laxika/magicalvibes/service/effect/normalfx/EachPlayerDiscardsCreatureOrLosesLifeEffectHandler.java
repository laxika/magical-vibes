package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsCreatureOrLosesLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsCreatureOrLosesLifeEffect;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves an APNAP discard flow followed by life loss for noncreature discards. The chosen cards are
 * discarded together once every player has chosen (CR 101.4).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsCreatureOrLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

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
            addInApnapOrder(gameData, state.remaining);
        } else if (state.currentPlayerId != null) {
            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
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
            gameData.deferredDiscardChooserId = playerId;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }

            state.currentPlayerId = null;
            gameData.deferredDiscardChooserId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }

        finish(gameData, entry, effect, state);
    }

    private void finish(GameData gameData, StackEntry entry,
                        EachPlayerDiscardsCreatureOrLosesLifeEffect effect,
                        EachPlayerDiscardsCreatureOrLosesLifeState state) {
        Map<UUID, List<Card>> discarded =
                cardChoiceHandlerService.discardDeferredSelections(gameData, entry.getControllerId());
        List<UUID> lifeLossPlayers = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (discarded.getOrDefault(playerId, List.of()).stream()
                    .noneMatch(card -> card.hasType(CardType.CREATURE))) {
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
