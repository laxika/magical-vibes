package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTriggeringPermanentControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTriggeringPermanentControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTriggeringPermanentControllerEffect) effect;

        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
