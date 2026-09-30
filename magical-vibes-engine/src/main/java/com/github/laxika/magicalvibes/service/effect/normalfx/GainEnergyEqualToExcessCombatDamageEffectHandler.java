package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainEnergyEqualToExcessCombatDamageEffect;
import org.springframework.stereotype.Component;

@Component
public class GainEnergyEqualToExcessCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final EnergyCountersEffectHandler energyCountersEffectHandler;

    public GainEnergyEqualToExcessCombatDamageEffectHandler(
            EnergyCountersEffectHandler energyCountersEffectHandler) {
        this.energyCountersEffectHandler = energyCountersEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainEnergyEqualToExcessCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        energyCountersEffectHandler.resolve(gameData, entry,
                new EnergyCountersEffect(entry.getEventValue()));
    }
}
