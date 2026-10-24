package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedCreatureEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean granted = ((SacrificeEnchantedCreatureEffect) effect).grantedToCreature();

        // Find the Aura permanent via sourcePermanentId. While the Aura is still on the battlefield
        // its current attachment is what "it" refers to at resolution (CR 608.2h); once the Aura has
        // left, the attachment it had when it left (its snapshot) is used. A granted ability belongs
        // to the creature itself, which is then the source.
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent auraSnapshot = entry.getSourcePermanentSnapshot();

        UUID enchantedId = granted
                ? entry.getSourcePermanentId()
                : auraPerm != null
                ? auraPerm.getAttachedTo()
                : auraSnapshot == null ? null : auraSnapshot.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping sacrifice trigger",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping sacrifice",
                    gameData.id);
            return;
        }

        UUID sacrificingPlayerId = granted ? entry.getControllerId() : entry.getTriggeringPermanentControllerId();
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, enchantedId);
        if (sacrificingPlayerId == null) {
            sacrificingPlayerId = currentControllerId;
        }
        if (sacrificingPlayerId == null || !sacrificingPlayerId.equals(currentControllerId)
                || gameQueryService.cantBeSacrificed(gameData, enchantedCreature)
                || !gameQueryService.canEffectCauseSacrifice(gameData, sacrificingPlayerId, entry.getControllerId())) {
            return;
        }

        stepDepartureSupport.sacrifice(gameData, entry, effect, enchantedId);
    }
}
