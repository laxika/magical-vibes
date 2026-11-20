package com.github.laxika.magicalvibes.service.effect.staticfx;

import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMulticoloredEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProtectionFromMulticoloredEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromMulticoloredEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var protection = (ProtectionFromMulticoloredEffect) effect;
        if (protection.subject() != null
                && support.namesBeyondSource(context, protection.subject(), null)) {
            accumulator.addGrantedEffect(protection);
        }
    }
}
