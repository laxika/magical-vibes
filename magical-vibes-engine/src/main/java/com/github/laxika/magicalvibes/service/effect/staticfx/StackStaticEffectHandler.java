package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WrappedStackStaticEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackStaticEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectHandlerRegistry registry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WrappedStackStaticEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardEffect wrapped = ((WrappedStackStaticEffect) effect).wrapped();
        StaticEffectHandler handler = registry.getHandler(wrapped);
        if (handler != null) {
            handler.apply(context, wrapped, accumulator);
        }
    }
}
