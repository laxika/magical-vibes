package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventDividedDamageEffect} (Remedy): adds a "next X damage" prevention shield to
 * each target per the controller-announced split on {@code StackEntry.damageAssignments}. Mirrors
 * {@link PreventDamageToTargetEffectHandler} but applies one shield per assigned target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventDividedDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDividedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PreventDividedDamageEffect prevention = (PreventDividedDamageEffect) effect;
        Map<UUID, Integer> assignments;
        if (prevention.etbAssignments()) {
            assignments = gameData.pendingETBDamageAssignments;
            gameData.pendingETBDamageAssignments = Map.of();
        } else {
            assignments = entry.getDamageAssignments();
        }
        if (assignments == null || assignments.isEmpty()) return;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int expectedAmount = amountEvaluationService.evaluate(gameData, prevention.amount(),
                AmountContext.forStackEntry(entry, source));
        int assignedAmount = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (assignedAmount != expectedAmount) return;

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            if (!prevention.etbAssignments() && !entry.isAssignmentTargetLegal(targetId)) continue;

            int amount = assignment.getValue();
            if (amount <= 0) continue;

            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                if (prevention.etbAssignments() && !damageSupport.isAnyTargetDamageRecipient(gameData, target)) continue;
                target.setDamagePreventionShield(target.getDamagePreventionShield() + amount);
                gameLogService.append(gameData, GameLog.textCardText("The next " + amount + " damage that would be dealt to ", target.getCard(), " is prevented."));
                continue;
            }

            if (gameData.playerIds.contains(targetId)) {
                int current = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
                gameData.playerDamagePreventionShields.put(targetId, current + amount);
                gameLogService.append(gameData, GameLog.text("The next " + amount + " damage that would be dealt to "
                                + gameData.playerIdToName.get(targetId) + " is prevented."));
            }
        }
    }
}
