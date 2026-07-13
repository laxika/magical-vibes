package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicTransferEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PsychicTransferEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

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
            gameBroadcastService.logAndBroadcast(gameData,
                    "Life totals differ by more than 5. Life totals aren't exchanged.");
            return;
        }

        // CR 118.7: If either player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, controller)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(controller) + "'s life total can't change. Exchange doesn't occur.");
            return;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(target) + "'s life total can't change. Exchange doesn't occur.");
            return;
        }

        String controllerName = gameData.playerIdToName.get(controller);
        String targetName = gameData.playerIdToName.get(target);

        if (controllerLife == targetLife) {
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " and " + targetName + " exchange life totals (both at " + controllerLife + ").");
            return;
        }

        // Per CR 119.7e: an exchange is implemented as each player gaining or losing life.
        // A player who can't gain life can't move to a higher life total via exchange.
        boolean controllerWouldGain = targetLife > controllerLife;
        boolean targetWouldGain = controllerLife > targetLife;
        boolean controllerCantGain = controllerWouldGain && !gameQueryService.canPlayerGainLife(gameData, controller);
        boolean targetCantGain = targetWouldGain && !gameQueryService.canPlayerGainLife(gameData, target);

        if (controllerCantGain && targetCantGain) {
            gameBroadcastService.logAndBroadcast(gameData,
                    controllerName + " and " + targetName + " can't gain life. Exchange doesn't occur.");
            return;
        }

        int newControllerLife = controllerCantGain ? controllerLife : targetLife;
        int newTargetLife = targetCantGain ? targetLife : controllerLife;

        if (controllerCantGain) {
            gameBroadcastService.logAndBroadcast(gameData, controllerName + " can't gain life.");
        }
        if (targetCantGain) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + " can't gain life.");
        }

        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " and " + targetName + " exchange life totals (" + controllerName + ": "
                        + controllerLife + " -> " + newControllerLife + ", " + targetName + ": "
                        + targetLife + " -> " + newTargetLife + ").");

        gameData.playerLifeTotals.put(controller, newControllerLife);
        gameData.playerLifeTotals.put(target, newTargetLife);

        if (newControllerLife > controllerLife) {
            triggerCollectionService.checkLifeGainTriggers(gameData, controller, newControllerLife - controllerLife);
        } else if (newControllerLife < controllerLife) {
            triggerCollectionService.checkLifeLossTriggers(gameData, controller, controllerLife - newControllerLife);
        }
        if (newTargetLife > targetLife) {
            triggerCollectionService.checkLifeGainTriggers(gameData, target, newTargetLife - targetLife);
        } else if (newTargetLife < targetLife) {
            triggerCollectionService.checkLifeLossTriggers(gameData, target, targetLife - newTargetLife);
        }

        log.info("Game {} - {} and {} exchange life totals ({} -> {}, {} -> {})",
                gameData.id, controllerName, targetName, controllerLife, newControllerLife, targetLife, newTargetLife);
    }
}
