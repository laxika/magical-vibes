package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleEnchantedPermanentIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a Watery Grasp-style shuffle of the permanent enchanted by an Aura. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleEnchantedPermanentIntoOwnerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleEnchantedPermanentIntoOwnerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "Enchanted creature" is read as the ability resolves: the Aura's current attachment, falling back to the
        // creature it was attached to when activated once the Aura has left the battlefield (CR 608.2h, 113.7a).
        UUID enchantedId = entry.getTargetId();
        Permanent aura = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        if (aura != null && aura.getAttachedTo() != null) {
            enchantedId = aura.getAttachedTo();
        }
        if (enchantedId == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchanted == null) {
            return;
        }

        String name = enchanted.getCard().getName();
        if (permanentRemovalService.removePermanentToLibraryShuffled(gameData, enchanted)) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
