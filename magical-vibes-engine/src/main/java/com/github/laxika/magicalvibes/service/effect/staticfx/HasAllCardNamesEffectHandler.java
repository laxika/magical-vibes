package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HasAllCardNamesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * The all-names characteristic is queried directly from the card in every zone. It has no single
 * battlefield name to write into the layered characteristic state, so the layer handler is a
 * deliberate no-op while still registering the static effect with the engine.
 */
@Component
public class HasAllCardNamesEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HasAllCardNamesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        // The characteristic applies in all zones; Card.hasAllCardNames() is the source of truth.
    }
}
