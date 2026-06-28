package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class BoostCreaturesOfChosenColorEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesOfChosenColorEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenColorEffect) effect;
        CardColor chosenColor = context.source().getChosenColor();
        if (chosenColor == null) return;
        if (!context.targetOnSameBattlefield()) return;
        Permanent target = context.target();
        boolean colorMatch = false;
        if (target.isColorOverridden()) {
            colorMatch = target.getTransientColors().contains(chosenColor);
        } else {
            CardColor effectiveColor = target.getEffectiveColor();
            colorMatch = (effectiveColor != null && effectiveColor == chosenColor)
                    || target.getTransientColors().contains(chosenColor);
        }
        if (colorMatch) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }
}
