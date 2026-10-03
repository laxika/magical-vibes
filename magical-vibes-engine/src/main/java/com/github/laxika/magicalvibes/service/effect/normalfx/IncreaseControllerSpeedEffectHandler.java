package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseControllerSpeedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the inherent speed trigger after players have had the opportunity to respond. */
@Component
@RequiredArgsConstructor
public class IncreaseControllerSpeedEffectHandler implements NormalEffectHandlerBean {
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseControllerSpeedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int speed = gameData.playerSpeeds.getOrDefault(entry.getControllerId(), 0);
        if (speed < 1 || speed >= 4) return;
        gameData.playerSpeeds.put(entry.getControllerId(), speed + 1);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " increases their speed to " + (speed + 1) + "."));
    }
}
