package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceFirstMatchingSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReduceFirstMatchingSpellCastCostEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceFirstMatchingSpellCastCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId())
                || !context.castingPlayerId().equals(context.gameData().activePlayerId)) {
            return 0;
        }

        var reduction = (ReduceFirstMatchingSpellCastCostEffect) effect;
        UUID sourceCardId = source.sourcePermanent() == null
                ? null
                : source.sourcePermanent().getCard().getId();
        if (!matches(reduction.predicate(), context.spell(), context, sourceCardId)) {
            return 0;
        }

        boolean matchingSpellAlreadyCast = context.gameData()
                .getSpellsCastThisTurn(context.castingPlayerId()).stream()
                .anyMatch(card -> matches(reduction.predicate(), card, context, sourceCardId));
        return matchingSpellAlreadyCast ? 0 : -reduction.amount();
    }

    private boolean matches(CardPredicate predicate, Card card, CostModificationContext context,
                            UUID sourceCardId) {
        return predicateEvaluationService.matchesCardPredicate(card, predicate, sourceCardId,
                context.gameData(), context.castingPlayerId());
    }
}
