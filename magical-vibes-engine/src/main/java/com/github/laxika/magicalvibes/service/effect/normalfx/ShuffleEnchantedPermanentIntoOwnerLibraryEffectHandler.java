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
        if (entry.getTargetId() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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
