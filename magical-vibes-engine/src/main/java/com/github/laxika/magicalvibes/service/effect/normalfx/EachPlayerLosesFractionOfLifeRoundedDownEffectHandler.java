package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedDownEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerLosesFractionOfLifeRoundedDownEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesFractionOfLifeRoundedDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesFractionOfLifeRoundedDownEffect) effect;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int lifeLoss = gameData.getLife(playerId) / e.divisor();
            if (lifeLoss > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        }
    }
}
