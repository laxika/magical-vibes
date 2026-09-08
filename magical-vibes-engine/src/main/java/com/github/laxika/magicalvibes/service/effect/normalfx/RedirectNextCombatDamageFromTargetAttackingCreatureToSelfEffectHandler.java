package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextCombatDamageFromTargetAttackingCreatureToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null
                || !gameQueryService.isCreature(gameData, source)
                || !gameQueryService.isCreature(gameData, target)
                || !target.isAttacking()) {
            return;
        }

        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                sourceId, targetId, CreatureDamageRedirectShield.NEXT_EVENT, targetId, true));
    }
}
