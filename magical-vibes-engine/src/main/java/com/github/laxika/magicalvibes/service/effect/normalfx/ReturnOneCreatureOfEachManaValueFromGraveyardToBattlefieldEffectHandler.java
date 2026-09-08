package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        for (int manaValue : e.manaValues()) {
            if (graveyard == null || graveyard.isEmpty()) {
                break;
            }

            CardPredicate filter = creatureWithManaValue(manaValue);
            List<Card> matching = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, null))
                    .toList();

            if (matching.isEmpty()) {
                continue;
            }

            if (matching.size() == 1) {
                Card card = matching.getFirst();
                graveyard.remove(card);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, card);
                graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, card);
            } else {
                gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                        controllerId, 1, filter, GraveyardChoiceDestination.BATTLEFIELD, false, true, false));
            }
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }

    private CardPredicate creatureWithManaValue(int manaValue) {
        return new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMinManaValuePredicate(manaValue),
                new CardMaxManaValuePredicate(manaValue)
        ));
    }
}
