package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TrackedLandsBecomeForestEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link TrackedLandsBecomeForestEffect}: every land whose id the source permanent recorded
 * in {@code forestedLandIds} (via Gaea's Liege's {@code {T}} ability) becomes a Forest, replacing
 * its other land types and mana ability per CR 305.7 (like Blood Moon / Evil Presence). Because the
 * source's {@code forestedLandIds} vanish with the source when it leaves the battlefield, so does
 * the Forest grant — matching "until this creature leaves the battlefield".
 */
@Component
public class TrackedLandsBecomeForestEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TrackedLandsBecomeForestEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (context.source().getForestedLandIds().contains(context.target().getId())) {
            accumulator.addGrantedSubtype(CardSubtype.FOREST);
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
