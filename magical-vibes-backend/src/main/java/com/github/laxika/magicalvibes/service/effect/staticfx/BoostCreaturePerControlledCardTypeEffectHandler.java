package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerControlledCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostCreaturePerControlledCardTypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturePerControlledCardTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerControlledCardTypeEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        int count = support.countControlledPermanents(context, p -> p.getCard().hasType(boost.cardType()));

        accumulator.addPower(count * boost.powerPerMatch());
        accumulator.addToughness(count * boost.toughnessPerMatch());
    }
}
