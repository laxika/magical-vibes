package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handler for {@link SkipNextUntapStepEffect}: queues one skipped untap step on the target player.
 * {@code TurnProgressionService.advanceTurn} consumes it when that player next becomes active.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextUntapStepEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextUntapStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        gameData.skipNextUntapStepCount.merge(targetPlayerId, 1, Integer::sum);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " skips their next untap step."));
        log.info("Game {} - {} skips their next untap step", gameData.id, playerName);
    }
}
