package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForNextMatchingSpellEffectHandler implements CostModificationHandlerBean {

    private final ReduceCastCostForMatchingSpellsEffectHandler matchingSpellsHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForNextMatchingSpellEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduction = (ReduceCastCostForNextMatchingSpellEffect) effect;
        return matchingSpellsHandler.modifyCost(context,
                new ReduceCastCostForMatchingSpellsEffect(
                        reduction.predicate(), reduction.amount(), CostModificationScope.SELF), source);
    }
}
