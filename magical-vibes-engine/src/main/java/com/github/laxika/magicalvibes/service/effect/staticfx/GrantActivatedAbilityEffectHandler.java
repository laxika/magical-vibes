package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import java.util.UUID;
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
            accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
        }
    }
}
