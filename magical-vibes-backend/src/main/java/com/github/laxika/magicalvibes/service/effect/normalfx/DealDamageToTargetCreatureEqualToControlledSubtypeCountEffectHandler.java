package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect) effect;

        int count = gameQueryService.countControlledSubtypePermanents(gameData, entry.getControllerId(), e.subtype());
        damageSupport.resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, count, entry));

        if (e.gainLife() && count > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), count);
        }
    
    }
}
