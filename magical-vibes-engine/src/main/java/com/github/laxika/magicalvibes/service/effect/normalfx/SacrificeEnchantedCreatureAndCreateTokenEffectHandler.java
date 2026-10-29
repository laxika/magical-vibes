package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedCreatureAndCreateTokenEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst, ContinuesAfterDeparture {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedCreatureAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The Aura may already have left (or its creature may have): the ability still resolves using
        // last-known information, and "you create a token" does not depend on the sacrifice.
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID enchantedId = null;
        if (auraPerm != null) {
            enchantedId = auraPerm.getAttachedTo();
        } else {
            Permanent auraSnapshot = entry.getSourcePermanentSnapshot();
            if (auraSnapshot != null) {
                enchantedId = auraSnapshot.getAttachedTo() != null
                        ? auraSnapshot.getAttachedTo() : auraSnapshot.getLastAttachedTo();
            }
            if (enchantedId == null && entry.getAttachedPermanentSnapshot() != null) {
                enchantedId = entry.getAttachedPermanentSnapshot().getId();
            }
        }

        Permanent enchantedCreature = enchantedId == null
                ? null : gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature of {} is gone, no sacrifice; token is still created",
                    gameData.id, entry.getCard().getName());
            continueAfterDeparture(gameData, entry, effect);
            return;
        }

        // Its controller sacrifices it (CR 701.21a), then you create the token.
        stepDepartureSupport.sacrificeThenContinue(gameData, entry, effect, enchantedId, this);
    }

    /** "… and you create a token": not contingent on the sacrifice. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeEnchantedCreatureAndCreateTokenEffect) effect;
        permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.tokenEffect(),
                entry.getCard().getSetCode());
    }
}
