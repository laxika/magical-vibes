package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaticBoostEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return StaticBoostEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        boolean applies = switch (boost.scope()) {
            case OWN_LANDS, OPPONENT_LANDS, ALL_LANDS, ALL_LANDS_INCLUDING_SELF ->
                    support.matchesLandScope(context, boost.scope(), boost.filter());
            default -> support.matchesCreatureScope(context, boost.scope(), boost.filter());
        };
        if (applies) {
            int multiplier = boost.scalingCounter() == null
                    ? 1
                    : (boost.scalingCounterOnTarget()
                            ? context.target().getCounterCount(boost.scalingCounter())
                            : context.source().getCounterCount(boost.scalingCounter()));
            accumulator.addPower(boost.powerBoost() * multiplier);
            accumulator.addToughness(boost.toughnessBoost() * multiplier);
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }
}
