package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerLosesFractionOfLifeRoundedUpEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesFractionOfLifeRoundedUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesFractionOfLifeRoundedUpEffect) effect;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int currentLife = gameData.getLife(playerId);
            int lifeLoss = (currentLife + e.divisor() - 1) / e.divisor();
            if (lifeLoss > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        }
    }
}
