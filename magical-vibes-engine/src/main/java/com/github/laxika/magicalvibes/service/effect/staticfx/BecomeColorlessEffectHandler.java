package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BecomeColorlessEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeColorlessEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomes = (BecomeColorlessEffect) effect;
        boolean matches = becomes.scope() == GrantScope.ALL_PERMANENTS
                ? support.matchesStaticFilter(context, context.target(), becomes.filter())
                : support.matchesCreatureScope(context, becomes.scope(), becomes.filter());
        if (matches) {
            accumulator.setColorOverriding(true);
        }
    }
}
