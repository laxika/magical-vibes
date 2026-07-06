package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealXDamageToAnyTargetAndGainXLifeEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealXDamageToAnyTargetAndGainXLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        int xValue = entry.getXValue();
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, xValue, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), xValue);

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
