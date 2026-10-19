package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCleanupTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextCleanupEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link ReturnSourceToHandAtNextCleanupEffect}: queues a delayed triggered ability that
 * {@code TurnCleanupService} puts on the stack at the beginning of the next cleanup step; it returns
 * the source permanent to its owner's hand when it resolves. Nothing happens if the source is no longer on the battlefield.
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
        gameData.queueDelayedAction(new DelayedCleanupTrigger(
                entry.getControllerId(), entry.getCard(),
                ReturnToHandEffect.allPermanentsMatching(
                        new PermanentIsSpecificPermanentPredicate(source.getId()))));
        log.info("Game {} - {} scheduled to return to hand at next cleanup", gameData.id, entry.getCard().getName());
    }
}
