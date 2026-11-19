package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAndLockPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersAndLockPermanentEffect} (Suncleanser): strips every counter of
 * every kind from the target permanent, then resolves into itself as a floating effect on it lasting for as long as
 * the source remains on the battlefield ({@link Duration#WHILE_SOURCE_REMAINS}; it ends for good as the source phases
 * out, CR 702.26f), so {@code GameQueryService.cantHaveCounters} refuses further counters while it lasts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersAndLockPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersAndLockPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int removed = 0;
        int oilRemoved = target.getCounterCount(CounterType.OIL);
        for (CounterType counterType : CounterType.values()) {
            // ANY and SILVER are wildcard categories, not concrete stored counters.
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += target.getCounterCount(counterType);
            target.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(target, oilRemoved);

        UUID sourceId = entry.getSourcePermanentId();
        // A "for as long as" lock whose source already left never begins (CR 611.2b).
        if (sourceId != null && gameQueryService.findPermanentById(gameData, sourceId) != null) {
            gameData.addEffectOnPermanent(entry.getCard().getName(), sourceId, entry.getControllerId(), effect,
                    target.getId(), Duration.WHILE_SOURCE_REMAINS);
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "All counters removed from ", target.getCard(), "; it can't have counters put on it."));
        log.info("Game {} - {} counter(s) removed from {} and counters locked by {}",
                gameData.id, removed, target.getCard().getName(), sourceId);
    }
}
