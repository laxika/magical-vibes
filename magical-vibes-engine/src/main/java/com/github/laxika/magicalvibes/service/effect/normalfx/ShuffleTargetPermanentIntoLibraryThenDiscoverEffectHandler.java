package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryThenDiscoverEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShuffleTargetPermanentIntoLibraryThenDiscoverEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPermanentIntoLibraryThenDiscoverEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();
        var targetOwnerId = gameData.defaultControllerOf(target.getId());
        String name = target.getCard().getName();
        if (permanentRemovalService.removePermanentToLibraryShuffled(gameData, target)) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled into owner's library", gameData.id, name);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (targetOwnerId == null) {
            return;
        }

        DiscoverEffect discover = new DiscoverEffect(manaValue);
        EffectHandler handler = effectHandlerRegistry.getHandler(discover);
        if (handler == null) {
            return;
        }

        var originalControllerId = entry.getControllerId();
        entry.setControllerId(targetOwnerId);
        try {
            handler.resolve(gameData, entry, discover);
        } finally {
            entry.setControllerId(originalControllerId);
        }
    }
}
