package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerLosesLifeAndControllerGainsLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerLosesLifeAndControllerGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerLosesLifeAndControllerGainsLifeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();

        // Target loses life
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s life total can't change.");
        } else {
            int targetCurrentLife = gameData.getLife(targetPlayerId);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - e.lifeLoss());

            String lossLog = targetName + " loses " + e.lifeLoss() + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lossLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, e.lifeLoss(), entry.getCard().getName());
        }

        // Controller gains life (skip if no life gain, e.g. pure life loss effects)
        if (e.lifeGain() > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, e.lifeGain());
        }
    }
}
