package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves a reanimation Aura's leaves-the-battlefield sacrifice (e.g. Animate Dead): the creature
 * that was enchanted by the Aura is sacrificed by its controller. The enchanted creature's permanent
 * ID was baked into the effect at trigger time (before the Aura left the battlefield).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedCreatureOnLeaveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedCreatureOnLeaveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeEnchantedCreatureOnLeaveEffect) effect;
        if (e.enchantedPermanentId() == null) {
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, e.enchantedPermanentId());
        if (enchantedCreature == null) {
            // Creature already left the battlefield (e.g. it died, which is what removed the Aura).
            log.info("Game {} - enchanted creature no longer on battlefield, skipping sacrifice", gameData.id);
            return;
        }

        String sacrificeLog = enchantedCreature.getCard().getName() + " is sacrificed ("
                + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(enchantedCreature.getCard(), " is sacrificed (", entry.getCard(), ")."));
        log.info("Game {} - {} sacrificed by {}", gameData.id,
                enchantedCreature.getCard().getName(), entry.getCard().getName());

        permanentRemovalService.removePermanentToGraveyard(gameData, enchantedCreature);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
