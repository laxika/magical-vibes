package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsGainChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Layer-5 additive color grant for "all permanents are the chosen color in addition to their other
 * colors" (Painter's Servant): each permanent (any controller, including lands) gains the source's
 * chosen color without replacing its existing colors. The source permanent itself is covered by
 * {@link AllPermanentsGainChosenColorSelfEffectHandler}.
 */
@Component
public class AllPermanentsGainChosenColorEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllPermanentsGainChosenColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardColor chosenColor = context.source().getChosenColor();
        if (chosenColor == null) return;
        accumulator.addGrantedColor(chosenColor);
    }
}
