package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnCardsFromControllerGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardsFromControllerGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnCardsFromControllerGraveyardToBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        int maxCount = amountEvaluationService.evaluate(gameData, e.maxCount(),
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
            if (e.manaValueEqualsX() && card.getManaValue() != entry.getXValue()) {
                continue;
            }
            if (e.maxTotalManaValue() != null && card.getManaValue() > e.maxTotalManaValue()) {
                continue;
            }
            if (predicateEvaluationService.matchesCardPredicate(card, e.filter(), null)) {
                matching.add(card);
            }
        }

        if (matching.isEmpty()) {
            return;
        }

        if (e.maxTotalManaValue() != null) {
            gameData.graveyardTargetOperation.resolutionTimeReturnCardsToBattlefieldResume = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                    controllerId, matching, maxCount,
                    "Choose up to " + maxCount + " matching cards with total mana value "
                            + e.maxTotalManaValue() + " or less from your graveyard.",
                    0, e.maxTotalManaValue()));
            return;
        }

        if (matching.size() <= maxCount) {
            // Auto-return all matching cards — no choice needed
            List<Card> cardsToReturn = new ArrayList<>();
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : matching) {
                    if (graveyard.remove(card)) {
                        graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                        cardsToReturn.add(card);
                    }
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
            if (!cardsToReturn.isEmpty()) {
                graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                        gameData, Map.of(controllerId, cardsToReturn), e.enterTapped(), null);
            }
            return;
        }

        // Controller has more matching cards than maxCount — prompt sequential "up to N" choices.
        gameData.pendingGraveyardReturnBatch = new PendingGraveyardReturnBatch(
                controllerId, List.of(), Map.of());
        gameData.pendingGraveyardReturnQueue.add(
                new PendingGraveyardReturnChoice(controllerId, maxCount, e.filter(),
                        GraveyardChoiceDestination.BATTLEFIELD, true, e.mandatory(), false));
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
