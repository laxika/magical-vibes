package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnCardsFromControllerGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

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
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                    controllerId, matching, e.maxCount(),
                    "Choose up to " + e.maxCount() + " matching cards with total mana value "
                            + e.maxTotalManaValue() + " or less from your graveyard.",
                    0, e.maxTotalManaValue()));
            return;
        }

        if (matching.size() <= e.maxCount()) {
            // Auto-return all matching cards — no choice needed
            List<String> returnedNames = new ArrayList<>();
            graveyardService.beginGraveyardLeaveBatch(gameData);
            try {
                for (Card card : matching) {
                    graveyard.remove(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                    graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, card);
                    returnedNames.add(card.getName());
                }
            } finally {
                graveyardService.endGraveyardLeaveBatch(gameData);
            }
            return;
        }

        // Controller has more matching cards than maxCount — prompt sequential "up to N" choices.
        gameData.pendingGraveyardReturnQueue.add(
                new PendingGraveyardReturnChoice(controllerId, e.maxCount(), e.filter(),
                        GraveyardChoiceDestination.BATTLEFIELD, true));
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
