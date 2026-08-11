package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectCombatDamageToTargetAttackingCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectCombatDamageToTargetAttackingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectCombatDamageToTargetAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (controllerId == null || target == null || !gameQueryService.isCreature(gameData, target)
                || !target.isAttacking()) {
            return;
        }

        gameData.turnDamageRedirectToCreatureShields.add(
                new TurnDamageRedirectToCreatureShield(controllerId, targetId, true, false));
    }
}
