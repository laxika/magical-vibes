package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromTargetPermanentOrPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromTargetPermanentOrPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        if (targetId == null) {
            entry.setEventValue(0);
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        int removed;
        String targetName;
        if (permanent != null) {
            removed = removePermanentCounters(gameData, permanent);
            targetName = permanent.getCard().getName();
        } else if (gameData.playerIdToName.containsKey(targetId)) {
            removed = removePlayerCounters(gameData, targetId);
            targetName = gameData.playerIdToName.get(targetId);
        } else {
            entry.setEventValue(0);
            return;
        }

        entry.setEventValue(removed);
        if (removed > 0) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" removes all counters from " + targetName + " (" + removed + ").")
                    .build());
        }
        log.info("Game {} - {} removes {} counter(s) from {}", gameData.id,
                entry.getCard().getName(), removed, targetName);
    }

    private int removePermanentCounters(GameData gameData, Permanent permanent) {
        int removed = 0;
        int oilRemoved = permanent.getCounterCount(CounterType.OIL);
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            removed += permanent.getCounterCount(counterType);
            permanent.setCounterCount(counterType, 0);
        }
        gameData.recordOilCounterRemoved(permanent, oilRemoved);
        return removed;
    }

    private int removePlayerCounters(GameData gameData, UUID playerId) {
        int removed = 0;
        removed += removeCounter(gameData.playerPoisonCounters, playerId);
        removed += removeCounter(gameData.playerRadCounters, playerId);
        removed += gameData.removePlayerEnergyCounters(playerId, Integer.MAX_VALUE);
        removed += removeCounter(gameData.playerSparkCounters, playerId);
        removed += removeCounter(gameData.playerExperienceCounters, playerId);
        return removed;
    }

    private int removeCounter(java.util.Map<UUID, Integer> counters, UUID playerId) {
        Integer removed = counters.remove(playerId);
        return removed == null ? 0 : removed;
    }
}
