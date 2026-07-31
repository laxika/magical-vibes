package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextCleanupEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ReturnSourceToHandAtNextCleanupEffect}: flags the source permanent so that
 * {@code TurnCleanupService} returns it to its owner's hand at the beginning of the next cleanup
 * step. Nothing happens if the source is no longer on the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceToHandAtNextCleanupEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceToHandAtNextCleanupEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        source.setReturnToHandAtNextCleanup(true);
        log.info("Game {} - {} scheduled to return to hand at next cleanup", gameData.id, entry.getCard().getName());
    }
}
