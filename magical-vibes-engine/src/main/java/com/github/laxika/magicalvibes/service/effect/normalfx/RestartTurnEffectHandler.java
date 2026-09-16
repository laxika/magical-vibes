package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RestartTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestartTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnProgressionService turnProgressionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RestartTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean restored = gameData.restoreTurnStartSnapshot();
        gameData.restartTurnRequested = true;
        gameLogService.append(gameData, GameLog.text("The turn restarts."));

        if (restored) {
            turnProgressionService.restartTurnFromBeginning(gameData);
            log.info("Game {} - Restart the turn effect resolved", gameData.id);
        } else {
            log.warn("Game {} - Restart the turn effect resolved without a turn-start snapshot",
                    gameData.id);
        }
    }
}
