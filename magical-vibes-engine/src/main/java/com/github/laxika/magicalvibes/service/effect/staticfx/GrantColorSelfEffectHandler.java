package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantColorSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantColorEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantColorEffect) effect;
        boolean matches = switch (grant.scope()) {
            case ALL_OWN_CREATURES, ALL_CREATURES_INCLUDING_SELF ->
                    support.matchesCreatureScope(context, grant.scope(), grant.filter());
            case SELF, SELF_AND_PAIRED, OWN_PERMANENTS ->
                    support.matchesStaticFilter(context, context.target(), grant.filter());
            default -> false;
        };
        if (matches) {
            accumulator.addGrantedColor(grant.color());
            if (grant.overriding()) {
                accumulator.setColorOverriding(true);
            }
        }
    }
}
