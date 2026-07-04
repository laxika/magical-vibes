package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class ReduceOwnCastCostForCardTypeEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostForCardTypeEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostForCardTypeEffect) effect;
        if (!source.controlledBy(context.castingPlayerId())) {
            return 0;
        }
        return reduce.affectedTypes().contains(context.spell().getType()) ? -reduce.amount() : 0;
    }
}
