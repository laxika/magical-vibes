package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicTransferEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PsychicTransferEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PsychicTransferEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controller = entry.getControllerId();
        UUID target = entry.getTargetId();
        if (controller == null || target == null) return;

        int controllerLife = gameData.getLife(controller);
        int targetLife = gameData.getLife(target);

        // "If the difference between your life total and target player's life total is 5 or less"
        if (Math.abs(controllerLife - targetLife) > 5) {
            gameLogService.append(gameData, GameLog.text("Life totals differ by more than 5. Life totals aren't exchanged."));
            return;
        }

        // CR 118.7: If either player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, controller)) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controller) + "'s life total can't change. Exchange doesn't occur."));
            return;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, target)) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(target) + "'s life total can't change. Exchange doesn't occur."));
            return;
        }

        String controllerName = gameData.playerIdToName.get(controller);
        String targetName = gameData.playerIdToName.get(target);

        if (controllerLife == targetLife) {
            gameLogService.append(gameData, GameLog.text(controllerName + " and " + targetName + " exchange life totals (both at " + controllerLife + ")."));
            return;
        }

        // Per CR 119.7e: an exchange is implemented as each player gaining or losing life.
        // A player who can't gain life can't move to a higher life total via exchange.
        boolean controllerWouldGain = targetLife > controllerLife;
        boolean targetWouldGain = controllerLife > targetLife;
        boolean controllerCantGain = controllerWouldGain && !gameQueryService.canPlayerGainLife(gameData, controller);
        boolean targetCantGain = targetWouldGain && !gameQueryService.canPlayerGainLife(gameData, target);

        if (controllerCantGain || targetCantGain) {
            gameLogService.append(gameData, GameLog.text(controllerName + " and " + targetName + " can't gain life. Exchange doesn't occur."));
            return;
        }

        lifeSupport.applySetLifeTotal(gameData, controller, targetLife);
        lifeSupport.applySetLifeTotal(gameData, target, controllerLife);

        int newControllerLife = gameData.getLife(controller);
        int newTargetLife = gameData.getLife(target);
        gameLogService.append(gameData, GameLog.text(controllerName + " and " + targetName + " exchange life totals (" + controllerName + ": "
                        + controllerLife + " -> " + newControllerLife + ", " + targetName + ": "
                        + targetLife + " -> " + newTargetLife + ")."));

        log.info("Game {} - {} and {} exchange life totals ({} -> {}, {} -> {})",
                gameData.id, controllerName, targetName, controllerLife, newControllerLife, targetLife, newTargetLife);
    }
}
