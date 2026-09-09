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
 * itself when the scope covers it ({@link GrantScope#SELF}, {@link GrantScope#SELF_AND_PAIRED},
 * {@link GrantScope#ALL_OWN_CREATURES}, {@link GrantScope#ALL_CREATURES_INCLUDING_SELF},
 * or {@link GrantScope#ALL_LANDS_INCLUDING_SELF}, filter
 * permitting). The non-self
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
        boolean applies = switch (grant.scope()) {
            case SELF, SELF_AND_PAIRED -> support.matchesStaticFilter(context, context.target(), grant.filter());
            case ALL_OWN_CREATURES, ALL_CREATURES_INCLUDING_SELF ->
                    support.matchesCreatureScope(context, grant.scope(), grant.filter());
            case ALL_LANDS_INCLUDING_SELF -> support.matchesLandScope(context, grant.scope(), grant.filter());
            default -> false;
        };
        if (applies) {
            accumulator.addActivatedAbility(grant.ability().withGrantSource(context.sourceId()));
        }
    }
}
