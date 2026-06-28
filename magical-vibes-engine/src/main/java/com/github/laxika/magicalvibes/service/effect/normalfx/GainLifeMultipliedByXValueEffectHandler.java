package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeMultipliedByXValueEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainLifeMultipliedByXValueEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeMultipliedByXValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifeMultipliedByXValueEffect) effect;
        int amount = entry.getXValue() * e.multiplier();
        if (amount <= 0) {
            return;
        }
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), amount, entry.getCard().getName());
    }
}
