package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerWithoutMaxSpeedEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves damage to every player who does not have max speed.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToEachPlayerWithoutMaxSpeedEffectHandler implements NormalEffectHandlerBean {

    private static final int MAX_SPEED = 4;

    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachPlayerWithoutMaxSpeedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEachPlayerWithoutMaxSpeedEffect) effect;
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (gameData.playerSpeeds.getOrDefault(playerId, 0) >= MAX_SPEED) {
                    continue;
                }
                int damage = e.damage();
                damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
