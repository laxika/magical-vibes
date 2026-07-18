package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeEnchantedCreatureAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeEnchantedCreatureAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeEnchantedCreatureAndCreateTokenEffect) effect;
        
                // Find the aura permanent via sourcePermanentId
                Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (auraPerm == null) {
                    log.info("Game {} - Aura {} no longer on battlefield, skipping sacrifice trigger",
                            gameData.id, entry.getCard().getName());
                    return;
                }

                // Find the enchanted creature
                UUID enchantedId = auraPerm.getAttachedTo();
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

                // Sacrifice the enchanted creature (its controller sacrifices it)
                String sacrificeLog = enchantedCreature.getCard().getName() + " is sacrificed ("
                        + entry.getCard().getName() + ").";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(enchantedCreature.getCard(), " is sacrificed (", entry.getCard(), ")."));
                log.info("Game {} - {} sacrificed by {}", gameData.id,
                        enchantedCreature.getCard().getName(), entry.getCard().getName());

                permanentRemovalService.removePermanentToGraveyard(gameData, enchantedCreature);
                permanentRemovalService.removeOrphanedAuras(gameData);

                // Create token for the aura's controller
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.tokenEffect(), entry.getCard().getSetCode());
    
    }
}
