package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
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
        boolean scopeMatch = switch (grant.scope()) {
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && support.matchesStaticFilter(context.target(), grant.filter());
            case SELF -> context.target().getId().equals(context.source().getId());
            case SELF_AND_PAIRED -> {
                UUID targetId = context.target().getId();
                UUID sourceId = context.source().getId();
                UUID pairedId = context.source().getPairedWithId();
                yield targetId.equals(sourceId) || (pairedId != null && targetId.equals(pairedId));
            }
            default -> support.matchesCreatureScope(context, grant.scope(), grant.filter());
        };
        if (scopeMatch) {
            accumulator.addGrantedEffect(grant);
        }
    }
}
