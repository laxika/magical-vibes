package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenPlayerModeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a mode-specific anthem based on the controller's choice stored on the source. */
@Component
@RequiredArgsConstructor
public class BoostCreaturesOfChosenPlayerModeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesOfChosenPlayerModeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenPlayerModeEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) return;

        var targetControllerId = context.gameData().findControllerOf(context.target());
        if (targetControllerId == null
                || !boost.mode().equals(context.source().getChosenModeByPlayer().get(targetControllerId))) {
            return;
        }

        accumulator.addPower(boost.powerBoost());
        accumulator.addToughness(boost.toughnessBoost());
    }
}
