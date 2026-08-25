package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaticBoostSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StaticBoostEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        if ((boost.scope() == GrantScope.SELF || boost.scope() == GrantScope.ALL_OWN_CREATURES
                || boost.scope() == GrantScope.ALL_CREATURES_INCLUDING_SELF)
                && support.matchesStaticFilter(context, context.target(), boost.filter())) {
            int multiplier = boost.scalingCounter() == null
                    ? 1
                    : context.source().getCounterCount(boost.scalingCounter());
            accumulator.addPower(boost.powerBoost() * multiplier);
            accumulator.addToughness(boost.toughnessBoost() * multiplier);
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }
}
