package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves a reanimation Aura's leaves-the-battlefield sacrifice (e.g. Animate Dead): the creature
 * that was enchanted by the Aura is sacrificed by its controller. The enchanted creature is the one the Aura was
 * attached to as it last existed on the battlefield, read from the stack entry's source snapshot.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "That creature": the creature the Aura was attached to as it last existed on the battlefield, else the
        // reanimated creature an Aura that could not attach chose.
        Permanent aura = entry.getSourcePermanentSnapshot();
        UUID enchantedPermanentId = aura == null ? null
                : aura.getAttachedTo() != null ? aura.getAttachedTo() : aura.getChosenPermanentId();
        if (enchantedPermanentId == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchantedCreature == null) {
            // Creature already left the battlefield (e.g. it died, which is what removed the Aura).
            log.info("Game {} - enchanted creature no longer on battlefield, skipping sacrifice", gameData.id);
            return;
        }

        stepDepartureSupport.sacrifice(gameData, entry, effect, enchantedCreature.getId());
    }
}
