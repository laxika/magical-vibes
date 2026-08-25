package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WrappedGraveyardStaticEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GraveyardStaticEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectHandlerRegistry registry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WrappedGraveyardStaticEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardEffect wrapped = ((WrappedGraveyardStaticEffect) effect).wrapped();
        StaticEffectHandler handler = registry.getHandler(wrapped);
        if (handler != null) {
            handler.apply(context, wrapped, accumulator);
        }
    }
}
