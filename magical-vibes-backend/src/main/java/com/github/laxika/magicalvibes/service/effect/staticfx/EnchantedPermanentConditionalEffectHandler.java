package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnchantedPermanentConditionalEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final StaticEffectHandlerRegistry staticEffectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentConditionalEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (EnchantedPermanentConditionalEffect) effect;
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }
        CardEffect activeEffect = support.matchesStaticFilter(context.target(), conditional.filter())
                ? conditional.ifMatch()
                : conditional.ifNotMatch();
        StaticEffectHandler handler = staticEffectHandlerRegistry.getHandler(activeEffect);
        if (handler != null) {
            handler.apply(context, activeEffect, accumulator);
        }
    }
}
