package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SelfBecomesCreatureOutsideBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Registers the all-zone characteristic marker with the static-effect system. Its effect is
 * intentionally consumed by non-battlefield card characteristic queries; it does not change the
 * source while the source is on the battlefield because the oracle condition excludes that zone.
 */
@Component
public class SelfBecomesCreatureOutsideBattlefieldEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SelfBecomesCreatureOutsideBattlefieldEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        // The marker applies only outside the battlefield. The layered static pass evaluates
        // battlefield permanents, so there is deliberately no accumulator contribution here.
    }
}
