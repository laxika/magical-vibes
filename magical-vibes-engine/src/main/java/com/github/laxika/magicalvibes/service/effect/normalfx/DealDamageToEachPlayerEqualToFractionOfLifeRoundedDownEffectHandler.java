package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect}: snapshot each
 * player's {@code floor(life / divisor)} damage, then deal (APNAP order).
 */
@Component
@RequiredArgsConstructor
public class DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachPlayerEqualToFractionOfLifeRoundedDownEffect) effect;
        if (e.divisor() <= 0) {
            return;
        }
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        Map<UUID, Integer> damageByPlayer = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            int amount = gameData.getLife(playerId) / e.divisor();
            if (amount > 0) {
                damageByPlayer.put(playerId, gameQueryService.applyDamageMultiplier(gameData, amount, entry));
            }
        }
        damageByPlayer.forEach((playerId, damage) ->
                damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage));

        gameOutcomeService.checkWinCondition(gameData);
    }
}
