package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

@FunctionalInterface
public interface StaticEffectHandler {
    void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator);
}

