package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenPlayerModeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the global mode-specific boost to the source creature itself. */
@Component
@RequiredArgsConstructor
public class BoostCreaturesOfChosenPlayerModeSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturesOfChosenPlayerModeEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesOfChosenPlayerModeEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) return;

        var sourceControllerId = context.gameData().findControllerOf(context.source());
        if (sourceControllerId == null
                || !boost.mode().equals(context.source().getChosenModeByPlayer().get(sourceControllerId))) {
            return;
        }

        accumulator.addPower(boost.powerBoost());
        accumulator.addToughness(boost.toughnessBoost());
    }
}
