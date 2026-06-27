package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class ProtectionFromColorsEffectHandler implements StaticEffectHandlerBean {
    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromColorsEffect) effect;
        if (context.source().isAttached()
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addProtectionColors(protection.colors());
        }
    }
}
