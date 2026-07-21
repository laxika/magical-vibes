package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectYourDamageToEnchantedCreatureThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Saving Grace's enters trigger: installs a turn-long shield redirecting all damage that
 * would be dealt to the aura's controller (and permanents they control) onto the creature the aura is
 * currently attached to. The destination creature is locked in here, so the redirect persists for the
 * rest of the turn even if the aura later leaves the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectYourDamageToEnchantedCreatureThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectYourDamageToEnchantedCreatureThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (controllerId == null) return;

        gameData.turnDamageRedirectToCreatureShields.add(
                new TurnDamageRedirectToCreatureShield(controllerId, creature.getId()));
    }
}
