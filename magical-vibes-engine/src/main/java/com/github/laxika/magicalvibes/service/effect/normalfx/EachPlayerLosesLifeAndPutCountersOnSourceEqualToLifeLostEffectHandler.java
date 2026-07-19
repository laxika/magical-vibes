package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect} (Blood Tyrant):
 * each player loses the fixed amount of life, and the source gains a +1/+1 counter for each 1 life
 * actually lost this way. The per-player loss is measured by comparing life before/after so a player
 * whose life total can't change contributes nothing to the counter count.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesLifeAndPutCountersOnSourceEqualToLifeLostEffect) effect;
        String sourceName = entry.getCard().getName();

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            int before = gameData.getLife(playerId);
            lifeSupport.applyLifeLoss(gameData, playerId, e.lifeLossPerPlayer(), sourceName);
            totalLifeLost += Math.max(0, before - gameData.getLife(playerId));
        }

        if (totalLifeLost <= 0 || entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source,
                CounterType.PLUS_ONE_PLUS_ONE, totalLifeLost);
    }
}
