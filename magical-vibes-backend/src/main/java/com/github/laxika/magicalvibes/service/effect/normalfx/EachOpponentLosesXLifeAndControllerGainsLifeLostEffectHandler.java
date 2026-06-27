package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachOpponentLosesXLifeAndControllerGainsLifeLostEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesXLifeAndControllerGainsLifeLostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int x = entry.getXValue();

        if (x <= 0) {
            return;
        }

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            lifeSupport.applyLifeLoss(gameData, playerId, x, entry.getCard().getName());
            totalLifeLost += x;
        }

        if (totalLifeLost > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }
}
