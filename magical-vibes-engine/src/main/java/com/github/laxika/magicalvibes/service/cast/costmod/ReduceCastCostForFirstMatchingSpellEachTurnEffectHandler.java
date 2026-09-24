package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForFirstMatchingSpellEachTurnEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

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
        if (reduce.kickedOnly() && !context.kicked()) {
            return 0;
        }

        var sourceCardId = source.sourcePermanent() == null ? null : source.sourcePermanent().getCard().getId();
        if (!predicateEvaluationService.matchesCardPredicate(
                context.spell(), reduce.predicate(), sourceCardId, context.gameData(), context.castingPlayerId())) {
            return 0;
        }
        var kickedSpellIds = context.gameData().getKickedSpellsCastThisTurn(context.castingPlayerId());
        boolean alreadyCastMatchingSpell = context.gameData().getSpellsCastThisTurn(context.castingPlayerId()).stream()
                .filter(spell -> !reduce.kickedOnly() || kickedSpellIds.contains(spell.getId()))
                .anyMatch(spell -> predicateEvaluationService.matchesCardPredicate(
                        spell, reduce.predicate(), sourceCardId, context.gameData(), context.castingPlayerId()));
        if (alreadyCastMatchingSpell) {
            return 0;
        }

        var amountContext = new AmountContext(context.castingPlayerId(), source.sourcePermanent(),
                null, 0, 0);
        return -amountEvaluationService.evaluate(context.gameData(), reduce.amount(), amountContext);
    }
}
