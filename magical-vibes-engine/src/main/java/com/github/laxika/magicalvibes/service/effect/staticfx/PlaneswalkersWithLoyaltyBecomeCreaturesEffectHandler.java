package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkersWithLoyaltyBecomeCreaturesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class PlaneswalkersWithLoyaltyBecomeCreaturesEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlaneswalkersWithLoyaltyBecomeCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var target = context.target();
        if (!target.getCard().hasType(CardType.PLANESWALKER)) {
            return;
        }
        int loyalty = target.getCounterCount(CounterType.LOYALTY);
        if (loyalty <= 0) {
            return;
        }
        accumulator.setAnimatedCreature(true);
        accumulator.setBasePTOverride(loyalty, loyalty);
        accumulator.setLosesAllAbilities(true);
    }
}
