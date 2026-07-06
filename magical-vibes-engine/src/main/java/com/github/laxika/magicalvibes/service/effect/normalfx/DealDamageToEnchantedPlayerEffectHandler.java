package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEnchantedPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEnchantedPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEnchantedPlayerEffect) effect;

        UUID targetId = e.affectedPlayerId();
        if (targetId == null || !gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int amount = e.damageEqualsAttachedCount() != null
                    ? damageSupport.countPermanentsAttachedToPlayer(gameData, targetId, e.damageEqualsAttachedCount())
                    : e.damage();
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, amount, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
