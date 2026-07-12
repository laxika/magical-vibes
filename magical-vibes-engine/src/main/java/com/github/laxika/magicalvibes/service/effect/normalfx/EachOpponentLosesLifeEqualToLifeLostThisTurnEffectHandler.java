package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToLifeLostThisTurn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link EachOpponentLosesLifeEqualToLifeLostThisTurn} (Wound Reflection): each opponent of
 * the ability's controller loses life equal to the life <em>that opponent</em> has lost this turn.
 * The amount is read per-opponent, so multiple opponents each lose their own total.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentLosesLifeEqualToLifeLostThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentLosesLifeEqualToLifeLostThisTurn.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        String sourceName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            int lifeLost = gameData.lifeLostThisTurn.getOrDefault(playerId, 0);
            if (lifeLost > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLost, sourceName);
            }
        }
    }
}
