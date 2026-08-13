package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect) effect;
        UUID endStepPlayerId = entry.getTargetId();
        if (!gameData.playerIds.contains(endStepPlayerId)) {
            return;
        }

        boolean opponentWasDealtDamage = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(endStepPlayerId))
                .anyMatch(playerId -> gameData.damageDealtToPlayersThisTurn.getOrDefault(playerId, 0) > 0);
        if (!opponentWasDealtDamage && !damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, endStepPlayerId, damage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
