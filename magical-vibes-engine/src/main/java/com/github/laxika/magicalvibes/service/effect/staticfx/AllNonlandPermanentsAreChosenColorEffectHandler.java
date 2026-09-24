package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.model.effect.AllNonlandPermanentsAreChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Layer-5 color setter for "all nonland permanents are the chosen color" (Shifting Sky): each
 * nonland permanent (any controller) becomes the source's chosen color, replacing its other
 * colors. The source permanent itself is covered by {@link
 * AllNonlandPermanentsAreChosenColorSelfEffectHandler}.
 */
@Component
@RequiredArgsConstructor
public class AllNonlandPermanentsAreChosenColorEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllNonlandPermanentsAreChosenColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        CardColor chosenColor = context.source().getChosenColor();
        if (chosenColor == null) return;
        if (support.matchesStaticLeaf(context.target(), new PermanentIsLandPredicate())) return;
        accumulator.addGrantedColor(chosenColor);
        accumulator.setColorOverriding(true);
    }
}
