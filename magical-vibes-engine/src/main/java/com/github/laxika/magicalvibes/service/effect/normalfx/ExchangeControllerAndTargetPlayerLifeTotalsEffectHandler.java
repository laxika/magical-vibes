package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControllerAndTargetPlayerLifeTotalsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExchangeControllerAndTargetPlayerLifeTotalsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControllerAndTargetPlayerLifeTotalsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        entry.setEventValue(0);
        if (controllerId == null || targetId == null) {
            return;
        }

        int controllerLife = gameData.getLife(controllerId);
        int targetLife = gameData.getLife(targetId);
        if (controllerLife == targetLife) {
            return;
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)
                || !gameQueryService.canPlayerLifeChange(gameData, targetId)) {
            gameLogService.append(gameData, GameLog.text("The life totals can't be exchanged."));
            return;
        }

        UUID gainingPlayerId = controllerLife < targetLife ? controllerId : targetId;
        if (!gameQueryService.canPlayerGainLife(gameData, gainingPlayerId)) {
            gameLogService.append(gameData, GameLog.text("The life totals can't be exchanged."));
            return;
        }

        lifeSupport.applySetLifeTotal(gameData, controllerId, targetLife);
        lifeSupport.applySetLifeTotal(gameData, targetId, controllerLife);
        entry.setEventValue(Math.max(0, controllerLife - gameData.getLife(controllerId)));
    }
}
