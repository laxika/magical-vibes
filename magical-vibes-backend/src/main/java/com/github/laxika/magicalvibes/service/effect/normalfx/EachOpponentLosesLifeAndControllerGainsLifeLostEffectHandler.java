package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachOpponentLosesLifeAndControllerGainsLifeLostEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesLifeAndControllerGainsLifeLostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentLosesLifeAndControllerGainsLifeLostEffect) effect;
        UUID controllerId = entry.getControllerId();
        int amount = e.amount();

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            lifeSupport.applyLifeLoss(gameData, playerId, amount, entry.getCard().getName());
            totalLifeLost += amount;
        }

        if (totalLifeLost > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }
}
