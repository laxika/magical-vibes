package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnSourceDamageRedirectToControllerShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectCombatDamageFromTargetUnblockedCreatureToItsControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID protectedPlayerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getTargetId();
        Permanent source = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (protectedPlayerId == null || source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        gameData.turnSourceDamageRedirectToControllerShields.add(
                new TurnSourceDamageRedirectToControllerShield(protectedPlayerId, sourcePermanentId));
    }
}
