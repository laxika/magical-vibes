package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveDefendingPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a non-targeting rad-counter effect against the player being attacked. */
@Component
@RequiredArgsConstructor
public class GiveDefendingPlayerRadCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveDefendingPlayerRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }

        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null || !gameData.playerIds.contains(defendingPlayerId)) {
            return;
        }

        GiveDefendingPlayerRadCountersEffect radEffect = (GiveDefendingPlayerRadCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, radEffect.amount(),
                AmountContext.forStackEntry(entry, null).withTargetPermanentId(defendingPlayerId));
        if (amount <= 0) {
            return;
        }

        gameData.playerRadCounters.merge(defendingPlayerId, amount, Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(defendingPlayerId, "Player");
        gameLogService.append(gameData,
                GameLog.text(playerName + " gets " + amount + " rad counter" + (amount == 1 ? "." : "s.")));
    }
}
