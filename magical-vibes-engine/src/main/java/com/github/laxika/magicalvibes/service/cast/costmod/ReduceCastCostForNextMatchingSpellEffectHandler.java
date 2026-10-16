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
                        reduction.predicate(), reduction.amount(), CostModificationScope.SELF,
                        java.util.Set.of(), false, reduction.faceDownOnly()), source);
    }

    /** The reduction applies to the spell's total cost, so it also lowers an alternate cost such as dash. */
    @Override
    public int modifyAlternateCost(CostModificationContext context, CardEffect effect,
                                   CostModificationSource source) {
        return modifyCost(context, effect, source);
    }
}
