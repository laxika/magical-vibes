package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnUntapLockedPermanentsEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link PutCountersOnUntapLockedPermanentsEffect}: places the counters on every permanent
 * the source currently holds a {@code WHILE_SOURCE_TAPPED} untap lock on. No-op when the source has
 * left the battlefield or untapped (an untapped source holds no locks).
 */
@Component
@RequiredArgsConstructor
public class PutCountersOnUntapLockedPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnUntapLockedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnUntapLockedPermanentsEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<Permanent> locked = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getUntapPreventedByPermanentIds().contains(sourcePermanentId)) {
                locked.add(p);
            }
        });
        for (Permanent p : locked) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, p, e.counterType(), e.count());
        }
    }
}
