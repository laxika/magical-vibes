package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class EnchantedPermanentBecomesTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentBecomesTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomesType = (EnchantedPermanentBecomesTypeEffect) effect;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addGrantedSubtype(becomesType.subtype());
            accumulator.setSubtypeOverriding(true);
            if (becomesType.isBasicLandSubtype()) {
                accumulator.setLandSubtypeOverriding(true);
            }
        }
    }
}
