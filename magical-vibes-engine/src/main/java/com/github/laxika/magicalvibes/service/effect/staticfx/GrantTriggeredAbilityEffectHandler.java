package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;

import java.util.EnumSet;
import java.util.Set;
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

    private static final Set<GrantScope> ATTACHMENT_SCOPES = EnumSet.of(
            GrantScope.ENCHANTED_CREATURE, GrantScope.ENCHANTED_PERMANENT, GrantScope.EQUIPPED_CREATURE);

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTriggeredAbilityEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantTriggeredAbilityEffect) effect;
        boolean scopeMatch = switch (grant.scope()) {
            case ALL_PERMANENTS -> !context.target().getId().equals(context.sourceId())
                    && support.matchesStaticFilter(context, context.target(), grant.filter());
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && support.matchesStaticFilter(context, context.target(), grant.filter());
            case SELF -> context.target().getId().equals(context.sourceId())
                    && support.matchesStaticFilter(context, context.target(), grant.filter());
            case SELF_AND_PAIRED -> {
                UUID targetId = context.target().getId();
                UUID sourceId = context.sourceId();
                UUID pairedId = context.source().getPairedWithId();
                yield targetId.equals(sourceId) || (pairedId != null && targetId.equals(pairedId));
            }
            default -> support.matchesCreatureScope(context, grant.scope(), grant.filter());
        };
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
        if (!ATTACHMENT_SCOPES.contains(grant.scope())
                || !(grant.grantedAbility() instanceof TriggeredAbility ability)) {
            return grant;
        }
        return new GrantTriggeredAbilityEffect(ability.withGrantingPermanentId(grantingPermanentId), grant.scope(),
                grant.filter());
    }

    /**
     * Makes a granted ability with a limit ("this ability triggers only once each turn") the instance the granting
     * permanent grants ({@link TriggeredAbility#instance()}): a permanent several permanents grant it to has an
     * instance from each, and each triggers on its own (CR 113.2c), so each keeps its own limit.
     */
    private static GrantTriggeredAbilityEffect asGrantedInstance(GrantTriggeredAbilityEffect grant,
                                                                 UUID grantingPermanentId) {
        if (!(grant.grantedAbility() instanceof TriggeredAbility ability)
                || ability.options().limit() == TriggerLimit.NONE) {
            return grant;
        }
        return new GrantTriggeredAbilityEffect(ability.withInstance("granted-by:" + grantingPermanentId),
                grant.scope(), grant.filter());
    }
}
