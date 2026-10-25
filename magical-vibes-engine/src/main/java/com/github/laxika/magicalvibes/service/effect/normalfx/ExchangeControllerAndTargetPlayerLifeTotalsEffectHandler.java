package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControllerAndTargetPlayerLifeTotalsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
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
@Component
@RequiredArgsConstructor
public class ExchangeControllerAndTargetPlayerLifeTotalsEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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
        UUID losingPlayerId = controllerLife < targetLife ? targetId : controllerId;
        if (!gameQueryService.canPlayerGainLife(gameData, gainingPlayerId)
                || !gameQueryService.canPlayerLoseLife(gameData, losingPlayerId)) {
            gameLogService.append(gameData, GameLog.text("The life totals can't be exchanged."));
            return;
        }

        Map<UUID, Integer> newLifeTotals = new LinkedHashMap<>();
        newLifeTotals.put(controllerId, targetLife);
        newLifeTotals.put(targetId, controllerLife);
        lifeSupport.askBeforeLifeTotalsChange(gameData, newLifeTotals);
        lifeSupport.applySetLifeTotal(gameData, controllerId, targetLife);
        lifeSupport.applySetLifeTotal(gameData, targetId, controllerLife);
        entry.setEventValue(Math.max(0, controllerLife - gameData.getLife(controllerId)));
    }
}
