package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantSubtypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantSubtypeEffect) effect;
        boolean matches = grant.scope() == GrantScope.ALL_PERMANENTS
                ? support.matchesStaticFilter(context.target(), grant.filter())
                : support.matchesCreatureScope(context, grant.scope(), null);
        if (matches) {
            accumulator.addGrantedSubtype(grant.subtype());
            if (grant.overriding()) {
                accumulator.setSubtypeOverriding(true);
            }
        }
    }
}
