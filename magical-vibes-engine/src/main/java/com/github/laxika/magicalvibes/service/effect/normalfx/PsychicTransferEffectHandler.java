package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PsychicTransferEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exchanges two players' life totals.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the exchange is its first change, and each player gains or loses the
 * life needed to reach the other's former total (CR 701.12c), so a Lich's Duel Mastery the losing player's loss would
 * sacrifice instead is asked about (CR 616.1) before either life total changes: the gaining player's change would
 * otherwise close the question window first.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PsychicTransferEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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

        // CR 701.12a: If either player's life total can't change, the exchange doesn't occur
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

        // CR 701.12c: an exchange is implemented as each player gaining or losing life. A player who
        // can't gain life can't be given a higher life total this way (CR 119.7), and one who can't lose
        // life can't be given a lower one (CR 119.8).
        UUID gainingPlayer = controllerLife < targetLife ? controller : target;
        UUID losingPlayer = controllerLife < targetLife ? target : controller;
        if (!gameQueryService.canPlayerGainLife(gameData, gainingPlayer)) {
            gameLogService.append(gameData, GameLog.text(controllerName + " and " + targetName + " can't gain life. Exchange doesn't occur."));
            return;
        }
        if (!gameQueryService.canPlayerLoseLife(gameData, losingPlayer)) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(losingPlayer) + " can't lose life. Exchange doesn't occur."));
            return;
        }

        Map<UUID, Integer> newLifeTotals = new LinkedHashMap<>();
        newLifeTotals.put(controller, targetLife);
        newLifeTotals.put(target, controllerLife);
        lifeSupport.askBeforeLifeTotalsChange(gameData, newLifeTotals);
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
