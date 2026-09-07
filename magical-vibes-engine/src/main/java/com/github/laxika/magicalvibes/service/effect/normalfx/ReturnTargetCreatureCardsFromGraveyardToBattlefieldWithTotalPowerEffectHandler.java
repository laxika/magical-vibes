package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Resolves Nethroi's all-or-nothing aggregate-power graveyard return. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect) effect;
        List<java.util.UUID> targetIds = entry.getTargetCardIds();
        Set<java.util.UUID> uniqueTargetIds = new HashSet<>(targetIds);
        if (targetIds.isEmpty() || uniqueTargetIds.size() != targetIds.size()) {
            return;
        }

        int totalPower = 0;
        for (java.util.UUID targetId : targetIds) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, targetId);
            if (card == null
                    || !entry.getControllerId().equals(gameQueryService.findGraveyardOwnerById(gameData, targetId))
                    || !predicateEvaluationService.matchesCardPredicate(
                    card, returnEffect.graveyardChoiceFilter(), entry.getCard().getId(), gameData,
                    entry.getControllerId(), null, null, entry.getXValue())) {
                return;
            }
            totalPower += card.getPower() == null ? 0 : card.getPower();
        }

        if (totalPower > returnEffect.maxTotalPower()) {
            return;
        }

        returnHandler.resolveForController(gameData, entry,
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        returnEffect.graveyardChoiceFilter(), Integer.MAX_VALUE, false, false),
                entry.getControllerId());
    }
}
