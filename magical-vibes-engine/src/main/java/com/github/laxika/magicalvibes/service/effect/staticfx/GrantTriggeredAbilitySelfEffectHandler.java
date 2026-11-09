package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
        // "Creatures you control have …" / "all creatures have …" include the source only while it is a creature
        // (an enchantment such as Pendrell Mists doesn't have the ability it grants).
        boolean applies = switch (grant.scope()) {
            case SELF, SELF_AND_PAIRED, OWN_PERMANENTS ->
                    support.matchesStaticFilter(context, context.target(), grant.filter());
            case ALL_OWN_CREATURES, ALL_CREATURES_INCLUDING_SELF ->
                    support.matchesCreatureScope(context, grant.scope(), grant.filter());
            default -> false;
        };
        if (applies) {
            accumulator.addGrantedEffect(grant);
        }
    }
}
