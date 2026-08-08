package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
 * every kind from the target permanent, then records it under the source permanent's id in
 * {@code GameData.countersLockedPermanentsWhileSourceOnBattlefield} so
 * {@code GameQueryService.cantHaveCounters} refuses further counters while the source stays.
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
        for (CounterType counterType : CounterType.values()) {
            // ANY and SILVER are wildcard categories, not concrete stored counters.
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += target.getCounterCount(counterType);
            target.setCounterCount(counterType, 0);
        }

        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId != null) {
            gameData.countersLockedPermanentsWhileSourceOnBattlefield
                    .computeIfAbsent(sourceId, id -> ConcurrentHashMap.newKeySet())
                    .add(target.getId());
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "All counters removed from ", target.getCard(), "; it can't have counters put on it."));
        log.info("Game {} - {} counter(s) removed from {} and counters locked by {}",
                gameData.id, removed, target.getCard().getName(), sourceId);
    }
}
