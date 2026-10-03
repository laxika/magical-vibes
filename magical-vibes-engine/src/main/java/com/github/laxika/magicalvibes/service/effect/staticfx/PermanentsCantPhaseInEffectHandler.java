package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsCantPhaseInEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Registers the phasing restriction with the static-effect system. The phasing turn-based action
 * consumes the marker directly because it does not modify permanent characteristics.
 */
@Component
public class PermanentsCantPhaseInEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PermanentsCantPhaseInEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        // The restriction is consumed by PhasingService, not by characteristic computation.
    }
}
