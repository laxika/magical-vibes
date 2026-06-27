package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourceToughnessToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageEqualToSourceToughnessToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageEqualToSourceToughnessToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) return;

        int toughness = gameQueryService.getEffectiveToughness(gameData, source);
        if (toughness <= 0) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, toughness, entry);
        damageSupport.resolveCreatureTargetDamage(gameData, entry, rawDamage);
    
    }
}
