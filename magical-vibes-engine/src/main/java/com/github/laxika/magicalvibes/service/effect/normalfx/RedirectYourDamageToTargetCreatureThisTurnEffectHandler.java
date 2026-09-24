package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToTargetCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Heroic Sacrifice's targeted, turn-long damage redirection. */
@Component
@RequiredArgsConstructor
public class RedirectYourDamageToTargetCreatureThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectYourDamageToTargetCreatureThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (controllerId == null || target == null
                || !gameQueryService.isCreature(gameData, target)
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, targetId))) {
            return;
        }

        gameData.turnDamageRedirectToCreatureShields.add(
                TurnDamageRedirectToCreatureShield.forControlledCreatures(controllerId, targetId));
    }
}
