package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileEnchantedCreatureEffect exileEffect = (ExileEnchantedCreatureEffect) effect;
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID enchantedId = exileEffect.enchantedPermanentId();
        if (enchantedId == null && auraPerm != null) {
            enchantedId = auraPerm.getAttachedTo();
        }
        if (enchantedId == null) {
            if (auraPerm == null) {
                log.info("Game {} - Aura {} no longer on battlefield, skipping exile trigger",
                        gameData.id, entry.getCard().getName());
            } else {
                log.info("Game {} - {} is not attached to anything, skipping exile",
                        gameData.id, entry.getCard().getName());
            }
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping exile", gameData.id);
            return;
        }

        gameLogService.append(gameData, GameLog.cardTextCard(enchantedCreature.getCard(), " is exiled (", entry.getCard(), ")."));
        log.info("Game {} - {} exiled by {}", gameData.id,
                enchantedCreature.getCard().getName(), entry.getCard().getName());

        permanentRemovalService.removePermanentToExile(gameData, enchantedCreature);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
