package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class IncreaseOpponentCastCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseOpponentCastCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var increase = (IncreaseOpponentCastCostEffect) effect;
        if (source.controlledBy(context.castingPlayerId())) {
            return 0;
        }
        return increase.affectedTypes().contains(context.spell().getType()) ? increase.amount() : 0;
    }
}
