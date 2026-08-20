package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToPermanentsWithCountersEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class GrantSupertypeToPermanentsWithCountersEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSupertypeToPermanentsWithCountersEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantSupertypeToPermanentsWithCountersEffect) effect;
        if (context.target().getCounterCount(grant.counterType()) > 0) {
            accumulator.addGrantedSupertype(grant.supertype());
        }
    }
}
