package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerSourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromTargetCreatureToSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectAllDamageFromTargetCreatureToSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageFromTargetCreatureToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID protectedPlayerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        UUID redirectTargetId = entry.getSourcePermanentId();
        if (protectedPlayerId == null || targetId == null || redirectTargetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        Permanent redirectTarget = gameQueryService.findPermanentById(gameData, redirectTargetId);
        if (target == null || !gameQueryService.isCreature(gameData, target) || !target.isAttacking()
                || redirectTarget == null) {
            return;
        }

        gameData.playerSourceDamageRedirectShields.add(
                new PlayerSourceDamageRedirectShield(protectedPlayerId, targetId, redirectTargetId));
    }
}
