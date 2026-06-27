package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControllerLifeTotalEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PowerToughnessEqualToControllerLifeTotalSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PowerToughnessEqualToControllerLifeTotalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 0);
        accumulator.addPower(lifeTotal);
        accumulator.addToughness(lifeTotal);
    }
}
