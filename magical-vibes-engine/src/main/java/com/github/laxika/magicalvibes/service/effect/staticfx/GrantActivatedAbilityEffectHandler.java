package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("staticGrantActivatedAbilityEffectHandler")
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityEffect) effect;
        boolean scopeMatch = support.names(context,
                StaticEffectSupport.withAllPermanentsIncludingSource(grant.scope()), grant.filter());
        if (scopeMatch) {
            accumulator.addActivatedAbility(grant.ability().withGrantSource(context.sourceId()));
        }
    }
}
