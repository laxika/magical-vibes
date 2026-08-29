package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnUntapLockedPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PutCountersOnUntapLockedPermanentsEffect}: places the counters on every permanent
 * snapshotted as locked when the draw-step trigger was created. The snapshot lets the trigger
 * resolve normally if the source leaves the battlefield or untaps afterward.
 */
@Component
@RequiredArgsConstructor
public class PutCountersOnUntapLockedPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PermanentCounterSupport permanentCounterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnUntapLockedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnUntapLockedPermanentsEffect) effect;
        for (UUID permanentId : entry.getEventCardIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, permanent, e.counterType(), e.count());
            }
        }
    }
}
