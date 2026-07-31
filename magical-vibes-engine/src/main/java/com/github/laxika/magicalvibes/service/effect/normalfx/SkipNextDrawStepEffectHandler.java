package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextDrawStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextDrawStepEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextDrawStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID affectedPlayerId = entry.getControllerId();
        if (affectedPlayerId == null || !gameData.playerIds.contains(affectedPlayerId)) {
            return;
        }

        gameData.skipNextDrawStepCount.merge(affectedPlayerId, 1, Integer::sum);

        String affectedName = gameData.playerIdToName.get(affectedPlayerId);
        gameLogService.append(gameData, GameLog.text(affectedName + " will skip their next draw step."));
        log.info("Game {} - {} will skip their next draw step", gameData.id, affectedName);
    }
}
