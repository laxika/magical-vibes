package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerDiscardsOneThenDrawsForEachCardTypeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Kefka's discard trigger, retaining all discarded card types across player choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsOneThenDrawsForEachCardTypeEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerDiscardsOneThenDrawsForEachCardTypeState state =
                gameData.eachPlayerDiscardsOneThenDrawsForEachCardType;
        EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect discardEffect =
                (EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect) effect;

        if (!state.active) {
            state.active = true;
            state.controllerId = entry.getControllerId();
            state.remaining.add(gameData.activePlayerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(gameData.activePlayerId)) {
                    state.remaining.add(playerId);
                }
            }
            beginNextDiscard(gameData, state);
        } else {
            if (state.currentPlayerId != null) {
                state.discardedCardTypes.addAll(gameData.lastDiscardedCardTypes);
                state.currentPlayerId = null;
            }
            beginNextDiscard(gameData, state);
        }

        if (!state.active) {
            int drawAmount = amountEvaluationService.evaluate(gameData,
                    discardEffect.drawnCardAmount(), AmountContext.forStackEntry(entry, null));
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), drawAmount);
        }
    }

    private void beginNextDiscard(GameData gameData,
            EachPlayerDiscardsOneThenDrawsForEachCardTypeState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            state.currentPlayerId = playerId;
            List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
            if (hand.isEmpty()) {
                state.currentPlayerId = null;
                continue;
            }

            gameData.discardCausedByOpponent = !playerId.equals(state.controllerId);
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            return;
        }

        state.active = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
