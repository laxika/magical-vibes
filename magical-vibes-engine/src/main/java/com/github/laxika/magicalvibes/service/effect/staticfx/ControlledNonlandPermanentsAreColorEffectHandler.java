package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledNonlandPermanentsAreColorEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Layer-5 color setter for "nonland permanents you control are [color]" (Celestial Dawn): each
 * nonland permanent the source's controller controls becomes that color, replacing its other colors
 * (CR 105.3). The source permanent itself is covered by
 * {@link ControlledNonlandPermanentsAreColorSelfEffectHandler}.
 */
@Component
public class ControlledNonlandPermanentsAreColorEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledNonlandPermanentsAreColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.targetOnSameBattlefield()) return;
        if (context.target().getCard().hasType(CardType.LAND)) return;
        accumulator.addGrantedColor(((ControlledNonlandPermanentsAreColorEffect) effect).color());
        accumulator.setColorOverriding(true);
    }
}
