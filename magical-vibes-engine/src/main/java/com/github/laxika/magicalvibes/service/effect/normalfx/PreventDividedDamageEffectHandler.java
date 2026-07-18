package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDividedDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) return;

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            int amount = assignment.getValue();

            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                target.setDamagePreventionShield(target.getDamagePreventionShield() + amount);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("The next " + amount + " damage that would be dealt to ", target.getCard(), " is prevented."));
                continue;
            }

            if (gameData.playerIds.contains(targetId)) {
                int current = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
                gameData.playerDamagePreventionShields.put(targetId, current + amount);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text("The next " + amount + " damage that would be dealt to "
                                + gameData.playerIdToName.get(targetId) + " is prevented."));
            }
        }
    }
}
