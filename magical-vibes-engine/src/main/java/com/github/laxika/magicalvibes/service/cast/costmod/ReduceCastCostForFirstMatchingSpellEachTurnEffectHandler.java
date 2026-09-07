package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForFirstMatchingSpellEachTurnEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForFirstMatchingSpellEachTurnEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceCastCostForFirstMatchingSpellEachTurnEffect) effect;
        if (!source.controlledBy(context.castingPlayerId())) {
            return 0;
        }

        var sourceCardId = source.sourcePermanent() == null ? null : source.sourcePermanent().getCard().getId();
        if (!predicateEvaluationService.matchesCardPredicate(
                context.spell(), reduce.predicate(), sourceCardId, context.gameData(), context.castingPlayerId())) {
            return 0;
        }
        boolean alreadyCastMatchingSpell = context.gameData().getSpellsCastThisTurn(context.castingPlayerId()).stream()
                .anyMatch(spell -> predicateEvaluationService.matchesCardPredicate(
                        spell, reduce.predicate(), sourceCardId, context.gameData(), context.castingPlayerId()));
        return alreadyCastMatchingSpell ? 0 : -reduce.amount();
    }
}
