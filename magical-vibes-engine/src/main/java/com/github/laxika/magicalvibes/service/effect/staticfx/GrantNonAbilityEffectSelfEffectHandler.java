package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The source's share of its own static rule over a set it belongs to ("each creature you control", Zilortha): the rule
 * reaches the source as {@link GrantNonAbilityEffectEffectHandler} reaches the rest of the set.
 */
@Component
@RequiredArgsConstructor
public class GrantNonAbilityEffectSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantNonAbilityEffectEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantNonAbilityEffectEffect) effect;
        if (support.names(context, grant.subject(), grant.filter())) {
            accumulator.addNonAbilityEffect(grant.effect());
        }
    }
}
