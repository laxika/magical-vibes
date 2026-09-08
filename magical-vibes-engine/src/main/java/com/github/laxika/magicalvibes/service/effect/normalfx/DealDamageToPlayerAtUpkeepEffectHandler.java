package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayerAtUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToPlayerAtUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPlayerAtUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DealDamageToPlayerAtUpkeepEffect damageEffect = (DealDamageToPlayerAtUpkeepEffect) effect;
        if (damageEffect.playerId() == null || !gameData.playerIds.contains(damageEffect.playerId())) {
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, 1, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, damageEffect.playerId(), damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
