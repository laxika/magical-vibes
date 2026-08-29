package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantEffectEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if (grant.scope() == GrantScope.SELF_AND_PAIRED) {
            UUID pairedId = context.source().getPairedWithId();
            if (pairedId != null && context.target().getId().equals(pairedId)) {
                accumulator.addGrantedEffect(grant.effect());
            }
            return;
        }
        boolean scopeMatch = switch (grant.scope()) {
            case ALL_PERMANENTS -> support.matchesStaticFilter(context, context.target(), grant.filter());
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && support.matchesStaticFilter(context, context.target(), grant.filter());
            default -> support.matchesCreatureScope(context, grant.scope(), grant.filter());
        };
        if (scopeMatch) {
            accumulator.addGrantedEffect(grant.effect());
        }
    }
}
