package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PowerToughnessEqualToCardsInAllGraveyardsSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PowerToughnessEqualToCardsInAllGraveyardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var ptEffect = (PowerToughnessEqualToCardsInAllGraveyardsEffect) effect;
        int count = support.countCardsInAllGraveyards(context.gameData(), ptEffect.filter());
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }
}
