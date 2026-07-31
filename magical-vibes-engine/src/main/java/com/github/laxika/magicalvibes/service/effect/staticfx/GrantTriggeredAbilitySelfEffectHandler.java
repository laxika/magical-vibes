package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self pass for {@link GrantTriggeredAbilityEffect}: the source permanent grants the triggered
 * ability to itself when the scope covers it (e.g. Tandem Lookout's soulbond grant reads "each
 * of those creatures"). The non-self {@link GrantTriggeredAbilityEffectHandler} never sees the
 * source as a target.
 */
@Component
@RequiredArgsConstructor
public class GrantTriggeredAbilitySelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTriggeredAbilityEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantTriggeredAbilityEffect) effect;
        if ((grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                || grant.scope() == GrantScope.ALL_OWN_CREATURES)
                && support.matchesStaticFilter(context, context.target(), grant.filter())) {
            accumulator.addGrantedEffect(grant);
        }
    }
}
