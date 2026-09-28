package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceEnchantedHandCardCastCostEffect;
import org.springframework.stereotype.Component;

/** Applies Don't Worry About It's one-generic-mana reduction to its attached hand card. */
@Component
public class ReduceEnchantedHandCardCastCostEffectHandler implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceEnchantedHandCardCastCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect,
                          CostModificationSource source) {
        if (source.sourcePermanent() == null
                || !source.controlledBy(context.castingPlayerId())
                || !source.sourcePermanent().isAttached()
                || !source.sourcePermanent().getAttachedTo().equals(context.spell().getId())) {
            return 0;
        }
        return -1;
    }
}
