package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self pass for {@link GrantActivatedAbilityEffect}: grants the ability to the source permanent
 * itself when the scope covers it ({@link GrantScope#SELF}, {@link GrantScope#SELF_AND_PAIRED}, or
 * {@link GrantScope#ALL_OWN_CREATURES}, filter permitting). The non-self
 * {@link GrantActivatedAbilityEffectHandler} is never invoked with source == target, so a lord
 * that also grants to itself — Manaweft Sliver giving every Sliver you control, itself included,
 * "{T}: Add one mana of any color." — needs this pass.
 */
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilitySelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityEffect) effect;
        if ((grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                || grant.scope() == GrantScope.ALL_OWN_CREATURES)
                && support.matchesStaticFilter(context.target(), grant.filter())) {
            accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
        }
    }
}
