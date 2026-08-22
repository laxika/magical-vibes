package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect) effect;
        List<Integer> groupSizes = entry.getTargetCardGroupSizes();
        if (groupSizes.size() != returnEffect.targetFilters().size()) {
            return;
        }

        int targetOffset = 0;
        for (int groupIndex = 0; groupIndex < groupSizes.size(); groupIndex++) {
            int groupSize = groupSizes.get(groupIndex);
            if (groupSize < 0 || targetOffset + groupSize > entry.getTargetCardIds().size()) {
                return;
            }

            List<UUID> groupTargetIds = entry.getTargetCardIds().subList(targetOffset, targetOffset + groupSize);
            targetOffset += groupSize;
            List<UUID> legalTargetIds = new ArrayList<>();
            for (UUID targetId : groupTargetIds) {
                Card card = gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of())
                        .stream().filter(candidate -> candidate.getId().equals(targetId)).findFirst().orElse(null);
                if (card != null && predicateEvaluationService.matchesCardPredicate(
                        card, returnEffect.targetFilters().get(groupIndex), entry.getCard().getId())) {
                    legalTargetIds.add(targetId);
                }
            }

            GraveyardChoiceDestination destination = returnEffect.destinations().get(groupIndex);
            if (destination == GraveyardChoiceDestination.BATTLEFIELD) {
                graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, legalTargetIds,
                        (graveyard, card) -> graveyardReturnSupport.putCardOntoBattlefield(
                                gameData, entry.getControllerId(), card, null, null),
                        " returns ", " from graveyard to the battlefield.");
            } else if (destination == GraveyardChoiceDestination.HAND) {
                graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry, legalTargetIds,
                        (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                        " returns ", " from graveyard to hand.");
            }
        }
    }
}
