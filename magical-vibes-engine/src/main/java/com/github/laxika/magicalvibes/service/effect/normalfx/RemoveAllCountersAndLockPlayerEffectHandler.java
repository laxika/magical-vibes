package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAndLockPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersAndLockPlayerEffect} (Suncleanser): clears every player counter
 * type the engine tracks (poison, energy, experience, rad, spark) from the target player, then records
 * that player under the source permanent's id in {@code GameData.countersLockedPlayersWhileSourceOnBattlefield}
 * so the counter-gain paths (e.g. {@code GameQueryService.canPlayerGetPoisonCounters} and
 * {@code GameQueryService.replaceEnergyCounters}) refuse further counters while the source remains on the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersAndLockPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersAndLockPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        gameData.playerPoisonCounters.remove(targetPlayerId);
        gameData.playerEnergyCounters.remove(targetPlayerId);
        gameData.playerExperienceCounters.remove(targetPlayerId);
        gameData.playerRadCounters.remove(targetPlayerId);
        gameData.playerSparkCounters.remove(targetPlayerId);

        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId != null) {
            gameData.countersLockedPlayersWhileSourceOnBattlefield
                    .computeIfAbsent(sourceId, id -> ConcurrentHashMap.newKeySet())
                    .add(targetPlayerId);
        }

        gameLogService.append(gameData,
                GameLog.text(entry.getCard().getName() + " removes all counters from the targeted player, who can't get counters."));
        log.info("Game {} - player {} loses all counters and can't get counters while {} remains",
                gameData.id, targetPlayerId, sourceId);
    }
}
