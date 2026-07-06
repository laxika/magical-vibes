package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        int count = gameQueryService.countControlledSubtypePermanents(gameData, entry.getControllerId(), e.subtype());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, count, entry);

        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Gain life equal to the damage amount (subtype count) if enabled
        if (e.gainLife() && count > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), count);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
