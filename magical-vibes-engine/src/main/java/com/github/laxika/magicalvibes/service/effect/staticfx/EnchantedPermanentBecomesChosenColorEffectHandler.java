package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesChosenColorEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class EnchantedPermanentBecomesChosenColorEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentBecomesChosenColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (context.source().getChosenColor() == null
                || !context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        accumulator.addGrantedColor(context.source().getChosenColor());
        accumulator.setColorOverriding(true);
    }
}
