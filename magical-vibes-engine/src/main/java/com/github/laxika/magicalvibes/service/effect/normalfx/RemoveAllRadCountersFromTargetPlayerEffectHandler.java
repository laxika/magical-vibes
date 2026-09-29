package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllRadCountersFromTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Removes all rad counters from the targeted player. */
@Component
@RequiredArgsConstructor
public class RemoveAllRadCountersFromTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllRadCountersFromTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }

        int radCounters = gameData.playerRadCounters.getOrDefault(targetId, 0);
        if (radCounters <= 0) {
            return;
        }

        gameData.playerRadCounters.put(targetId, 0);
        String playerName = gameData.playerIdToName.get(targetId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " loses all " + radCounters + " rad counters (" + entry.getCard().getName() + ")."));
    }
}
