package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostSelfPerOpponentPoisonCounterSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostSelfPerOpponentPoisonCounterEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerOpponentPoisonCounterEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        int totalPoison = 0;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                totalPoison += context.gameData().playerPoisonCounters.getOrDefault(playerId, 0);
            }
        }
        accumulator.addPower(totalPoison * boost.powerPerCounter());
        accumulator.addToughness(totalPoison * boost.toughnessPerCounter());
    }
}
