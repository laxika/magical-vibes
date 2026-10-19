package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfEachFilterFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Returns one card per filter from the controller's graveyard to their hand as a single
 * graveyard-leave event. Filters with exactly one matching card return it without a prompt; the
 * others are queued as sequential mandatory graveyard choices.
 */
@Component
@RequiredArgsConstructor
public class ReturnOneOfEachFilterFromControllerGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneOfEachFilterFromControllerGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnOneOfEachFilterFromControllerGraveyardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        if (!gameData.graveyardReturnQueueLeaveBatchOpen) {
            gameData.graveyardReturnQueueLeaveBatchOpen = true;
            graveyardService.beginGraveyardLeaveBatch(gameData);
        }
        for (CardPredicate filter : returnEffect.filters()) {
            List<Card> matching = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, filter, null, gameData, controllerId))
                    .toList();
            if (matching.size() == 1) {
                Card card = matching.getFirst();
                graveyard.remove(card);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                graveyardReturnSupport.moveCardToDestination(gameData, controllerId, card,
                        GraveyardChoiceDestination.HAND, null, null, false);
            } else if (matching.size() > 1) {
                gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                        controllerId, 1, filter, GraveyardChoiceDestination.HAND, false, true, false));
            }
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
