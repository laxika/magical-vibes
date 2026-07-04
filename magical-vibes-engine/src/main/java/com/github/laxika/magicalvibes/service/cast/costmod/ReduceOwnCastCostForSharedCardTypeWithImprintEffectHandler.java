package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSharedCardTypeWithImprintEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.cast.CostModificationSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceOwnCastCostForSharedCardTypeWithImprintEffectHandler implements CostModificationHandlerBean {

    private final CostModificationSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostForSharedCardTypeWithImprintEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceOwnCastCostForSharedCardTypeWithImprintEffect) effect;
        if (!source.controlledBy(context.castingPlayerId())) {
            return 0;
        }
        Card imprinted = source.sourcePermanent().getCard().getImprintedCard();
        if (imprinted == null || !support.sharesCardType(context.spell(), imprinted)) {
            return 0;
        }
        return -reduce.amount();
    }
}
