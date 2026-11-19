package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAndLockPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersAndLockPlayerEffect} (Suncleanser): clears every player counter
 * type the engine tracks (poison, energy, experience, rad, spark) from the target player, then resolves into itself
 * as a floating effect on that player lasting {@link Duration#WHILE_SOURCE_REMAINS} (it ends for good as the source
 * phases out, CR 702.26f), so the counter-gain paths (e.g. {@code GameQueryService.canPlayerGetPoisonCounters} and
 * {@code GameQueryService.replaceEnergyCounters}) refuse further counters while it lasts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersAndLockPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
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
        // A "for as long as" lock whose source already left never begins (CR 611.2b).
        if (sourceId != null && gameQueryService.findPermanentById(gameData, sourceId) != null) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(), sourceId,
                    entry.getControllerId(), effect, targetPlayerId, Duration.WHILE_SOURCE_REMAINS));
        }

        gameLogService.append(gameData,
                GameLog.text(entry.getCard().getName() + " removes all counters from the targeted player, who can't get counters."));
        log.info("Game {} - player {} loses all counters and can't get counters while {} remains",
                gameData.id, targetPlayerId, sourceId);
    }
}
