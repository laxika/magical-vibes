package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithEmptyHandLosesLifeEffect;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss limited to opponents with empty hands. */
@Component
@RequiredArgsConstructor
public class EachOpponentWithEmptyHandLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentWithEmptyHandLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentWithEmptyHandLosesLifeEffect lifeLoss =
                (EachOpponentWithEmptyHandLosesLifeEffect) effect;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId())
                    && gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss.amount(), entry.getCard().getName());
            }
        }
    }
}
