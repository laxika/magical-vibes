package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A source's static rule over the set its subject names (Zilortha, Stoic Star-Captain): each permanent named gets the
 * rule as a non-ability effect, which a "loses all abilities" on that permanent does not remove (CR 613.1f).
 */
@Component
@RequiredArgsConstructor
public class GrantNonAbilityEffectEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantNonAbilityEffectEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantNonAbilityEffectEffect) effect;
        if (support.namesBeyondSource(context, grant.subject(), grant.filter())) {
            accumulator.addNonAbilityEffect(grant.effect());
        }
    }
}
