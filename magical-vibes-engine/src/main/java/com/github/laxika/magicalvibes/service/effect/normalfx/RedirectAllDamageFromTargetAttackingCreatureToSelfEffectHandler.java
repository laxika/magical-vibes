package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromTargetAttackingCreatureToSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectAllDamageFromTargetAttackingCreatureToSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageFromTargetAttackingCreatureToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        UUID sourceId = entry.getSourcePermanentId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (controllerId == null || target == null || source == null
                || !gameQueryService.isCreature(gameData, target) || !target.isAttacking()
                || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        gameData.turnDamageRedirectToCreatureShields.add(
                new TurnDamageRedirectToCreatureShield(controllerId, sourceId, false, false, targetId));
    }
}
