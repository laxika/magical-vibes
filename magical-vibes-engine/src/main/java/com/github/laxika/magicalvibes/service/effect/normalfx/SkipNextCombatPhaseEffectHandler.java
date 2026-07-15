package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextCombatPhaseEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextCombatPhaseEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextCombatPhaseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID affectedPlayerId = entry.getTargetId();
        if (affectedPlayerId == null || !gameData.playerIds.contains(affectedPlayerId)) {
            return;
        }

        gameData.skipNextCombatPhaseCount.merge(affectedPlayerId, 1, Integer::sum);

        String affectedName = gameData.playerIdToName.get(affectedPlayerId);
        String logEntry = affectedName + " skips their next combat phase.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} will skip their next combat phase", gameData.id, affectedName);
    }
}
