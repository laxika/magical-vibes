package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrackedLandsBecomeBasicLandTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/** Applies source-bound basic land type changes in the type layer. */
@Component
public class TrackedLandsBecomeBasicLandTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrackedLandsBecomeBasicLandTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var e = (TrackedLandsBecomeBasicLandTypeEffect) effect;
        if (e.subtype() == null) {
            return;
        }
        if (context.source().getLandTypesUntilSourceLeaves()
                .get(context.target().getId()) == e.subtype()) {
            accumulator.addGrantedSubtype(e.subtype());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
