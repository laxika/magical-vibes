package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllCreatureTypesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the continuous layer-4 form of losing all creature types. */
@Component
@RequiredArgsConstructor
public class LoseAllCreatureTypesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseAllCreatureTypesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var lose = (LoseAllCreatureTypesEffect) effect;
        if (support.matchesCreatureScope(context, lose.scope(), lose.filter())) {
            accumulator.setSubtypeOverriding(true);
        }
    }
}
