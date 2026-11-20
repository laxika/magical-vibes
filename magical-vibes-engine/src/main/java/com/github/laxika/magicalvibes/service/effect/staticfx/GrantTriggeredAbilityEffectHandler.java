package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies {@link GrantTriggeredAbilityEffect}: the grant itself is accumulated as a granted
 * effect on the matching permanent, and trigger-collection sites read it back through
 * {@code GrantedTriggeredAbilitySupport}.
 */
@Component("staticGrantTriggeredAbilityEffectHandler")
@RequiredArgsConstructor
public class GrantTriggeredAbilityEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTriggeredAbilityEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantTriggeredAbilityEffect) effect;
        boolean scopeMatch = support.names(context, grant.scope().subject(), grant.filter());
        if (scopeMatch) {
            accumulator.addGrantedEffect(asGrantedByAttachment(asGrantedInstance(grant, context.sourceId()),
                    context.sourceId()));
        }
    }

    /**
     * Names the Aura or Equipment that grants an "enchanted / equipped creature has '…'" triggered ability as the
     * ability's granting permanent ({@link TriggeredAbility#grantingPermanentId()}), so "sacrifice this Equipment" and
     * "other than Animal Friend" in it mean that permanent, and two Equipment that grant the creature equal abilities
     * give it one instance from each.
     */
    private static GrantTriggeredAbilityEffect asGrantedByAttachment(GrantTriggeredAbilityEffect grant,
                                                                     UUID grantingPermanentId) {
        if (!(grant.scope().subject() instanceof PermanentSubject.Attached)
                || !(grant.grantedAbility() instanceof TriggeredAbility ability)) {
            return grant;
        }
        return new GrantTriggeredAbilityEffect(ability.withGrantingPermanentId(grantingPermanentId), grant.scope(),
                grant.filter());
    }

    /**
     * Makes a granted ability with a limit ("this ability triggers only once each turn") the instance its grantor
     * grants ({@link TriggeredAbility#instance()}): a permanent several grantors give it to has an instance from each,
     * and each triggers on its own (CR 113.2c), so each keeps its own limit. The grantor is the granting permanent
     * of a static grant, or the floating effect of a resolved one, so the same ability given twice until end of turn
     * is two instances too.
     */
    public static GrantTriggeredAbilityEffect asGrantedInstance(GrantTriggeredAbilityEffect grant, UUID grantorId) {
        if (!(grant.grantedAbility() instanceof TriggeredAbility ability)
                || ability.options().limit() == TriggerLimit.NONE) {
            return grant;
        }
        return new GrantTriggeredAbilityEffect(ability.withInstance("granted-by:" + grantorId),
                grant.scope(), grant.filter());
    }
}
