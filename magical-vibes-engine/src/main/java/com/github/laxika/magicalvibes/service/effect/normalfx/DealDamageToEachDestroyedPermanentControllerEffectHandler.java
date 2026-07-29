package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToEachDestroyedPermanentControllerEffect}: each player takes damage
 * equal to how many times they appear on {@code StackEntry.eventPlayerIds}, the per-permanent
 * controller tally stamped by the destroy handler that ran earlier on this entry. Players with no
 * entry there take no damage at all.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToEachDestroyedPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachDestroyedPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            int count = (int) entry.getEventPlayerIds().stream().filter(playerId::equals).count();
            if (count <= 0) {
                continue;
            }
            damageSupport.dealDamageToPlayer(gameData, entry, playerId,
                    gameQueryService.applyDamageMultiplier(gameData, count, entry));
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
