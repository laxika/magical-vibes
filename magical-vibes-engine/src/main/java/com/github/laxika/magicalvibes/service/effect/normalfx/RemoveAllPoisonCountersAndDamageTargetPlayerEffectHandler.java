package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllPoisonCountersAndDamageTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Removes every poison counter from the stack entry's target player and deals that many damage to
 * them (Leeches). The counter count is read before removal, so the damage is what the player had,
 * and zero counters means zero damage — the spell still resolves.
 */
@Component
@RequiredArgsConstructor
public class RemoveAllPoisonCountersAndDamageTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllPoisonCountersAndDamageTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) {
            return;
        }

        int poison = gameData.playerPoisonCounters.getOrDefault(targetId, 0);
        if (poison > 0) {
            gameData.playerPoisonCounters.put(targetId, 0);
            String playerName = gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " loses all " + poison + " poison counters (" + entry.getCard().getName() + ")."));
        }

        if (poison > 0 && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, poison, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
