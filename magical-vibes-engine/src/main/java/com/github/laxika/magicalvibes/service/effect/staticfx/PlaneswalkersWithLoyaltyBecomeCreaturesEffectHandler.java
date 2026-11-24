package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkersWithLoyaltyBecomeCreaturesEffect;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link PlaneswalkersWithLoyaltyBecomeCreaturesEffect} (Spark Rupture) for the static-bonus assembly and the
 * layer-6 harvest. Which planeswalkers it affects is decided in layer 4 against their layered types (CR 613.1d) and
 * kept per effect instance by the layered pass, since by layer 6 and 7b they are no longer planeswalkers (CR 613.6);
 * this handler reads that set. Without a layered board it has nothing to judge and does nothing.
 */
@Component
public class PlaneswalkersWithLoyaltyBecomeCreaturesEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlaneswalkersWithLoyaltyBecomeCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pass = LayerSystemService.activePass();
        if (pass == null || pass.board() == null
                || !pass.board().animatedBy(effect).contains(context.target().getId())) {
            return;
        }
        int loyalty = context.target().getCounterCount(CounterType.LOYALTY);
        accumulator.setAnimatedCreature(true);
        accumulator.setBasePTOverride(loyalty, loyalty);
        accumulator.setLosesAllAbilities(true);
    }
}
