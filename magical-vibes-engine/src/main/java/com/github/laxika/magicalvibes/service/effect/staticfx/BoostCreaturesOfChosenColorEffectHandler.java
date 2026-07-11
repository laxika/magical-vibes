package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
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
        boolean colorMatch;
        // Layer-5-aware while a CR 613 pass is active: the boost's color check (7c) reads the
        // colors decided in layer 5.
        CharacteristicState layered = LayerSystemService.activeStateFor(target.getId());
        if (layered != null) {
            colorMatch = layered.getColors().contains(chosenColor);
        } else if (target.isColorOverridden()) {
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
