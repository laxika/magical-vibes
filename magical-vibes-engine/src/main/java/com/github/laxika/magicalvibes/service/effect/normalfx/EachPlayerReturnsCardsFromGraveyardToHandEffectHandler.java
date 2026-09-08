package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerReturnsCardsFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerReturnsCardsFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (EachPlayerReturnsCardsFromGraveyardToHandEffect) effect;
        if (returnEffect.maxCount() <= 0) {
            return;
        }

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }

            List<Card> matching = new ArrayList<>();
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(card, returnEffect.filter(), null)) {
                    matching.add(card);
                }
            }
            if (matching.isEmpty()) {
                continue;
            }

            if (matching.size() <= returnEffect.maxCount()) {
                graveyardService.beginGraveyardLeaveBatch(gameData);
                try {
                    for (Card card : matching) {
                        if (graveyard.remove(card)) {
                            graveyardService.notifyCardsLeftGraveyard(gameData, playerId, card);
                            graveyardReturnSupport.moveCardToDestination(
                                    gameData, playerId, card, GraveyardChoiceDestination.HAND,
                                    null, null, false);
                        }
                    }
                } finally {
                    graveyardService.endGraveyardLeaveBatch(gameData);
                }
            } else {
                gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                        playerId, returnEffect.maxCount(), returnEffect.filter(),
                        GraveyardChoiceDestination.HAND, true, false, false));
            }
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            order.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
