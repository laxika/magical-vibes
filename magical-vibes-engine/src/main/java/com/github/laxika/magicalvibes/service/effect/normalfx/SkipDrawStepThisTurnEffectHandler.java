package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepThisTurnEffect;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a one-turn draw-step skip, collapsing repeated applications in that turn. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkipDrawStepThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipDrawStepThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null
                || !controllerId.equals(gameData.activePlayerId)
                || gameData.currentStep.ordinal() >= TurnStep.DRAW.ordinal()) {
            return;
        }

        gameData.skipDrawStepThisTurn.put(controllerId, gameData.turnNumber);
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " will skip their draw step this turn."));
        log.info("Game {} - {} will skip their draw step this turn", gameData.id, controllerName);
    }
}
