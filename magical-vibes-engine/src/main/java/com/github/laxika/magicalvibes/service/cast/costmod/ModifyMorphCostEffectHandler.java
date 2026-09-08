package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyMorphCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

@Component
public class ModifyMorphCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ModifyMorphCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyMorphCost(CostModificationContext context, CardEffect effect,
                               CostModificationSource source) {
        if (source.sourcePermanent() != null && source.sourcePermanent().isFaceDown()) {
            return 0;
        }
        ModifyMorphCostEffect modification = (ModifyMorphCostEffect) effect;
        boolean applies = switch (modification.scope()) {
            case SELF -> source.controlledBy(context.castingPlayerId());
            case OPPONENT -> !source.controlledBy(context.castingPlayerId());
            case ALL -> true;
        };
        return applies ? modification.amount() : 0;
    }
}
