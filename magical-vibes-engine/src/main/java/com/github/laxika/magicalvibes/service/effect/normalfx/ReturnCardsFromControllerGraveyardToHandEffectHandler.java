package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnCardsFromControllerGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardsFromControllerGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnCardsFromControllerGraveyardToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        int maxCount = amountEvaluationService.evaluate(gameData, returnEffect.maxCount(),
                AmountContext.forStackEntry(entry, null));
        if (maxCount <= 0) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        List<Card> matching = new ArrayList<>();
        for (Card card : graveyard) {
            if (predicateEvaluationService.matchesCardPredicate(card, returnEffect.filter(), null)) {
                matching.add(card);
            }
        }
        if (matching.isEmpty()) {
            return;
        }

        gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                controllerId, Math.min(maxCount, matching.size()), returnEffect.filter(),
                GraveyardChoiceDestination.HAND, true, false, false));
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
