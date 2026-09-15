package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtNextCleanupEffect;
import org.springframework.stereotype.Component;

import com.github.laxika.magicalvibes.model.action.DelayedCleanupTrigger;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Resolves {@link ExileCreatedPermanentsAtNextCleanupEffect} by scheduling each permanent created
 * earlier in the same resolution for exile during the next cleanup step.
 */
@Component
public class ExileCreatedPermanentsAtNextCleanupEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreatedPermanentsAtNextCleanupEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) return;
        var filter = new PermanentAnyOfPredicate(
                entry.getCreatedPermanentIds().stream()
                        .<PermanentPredicate>map(
                                PermanentIsSpecificPermanentPredicate::new)
                        .toList());
        gameData.queueDelayedAction(new DelayedCleanupTrigger(
                entry.getControllerId(), entry.getCard(),
                new ExileAllPermanentsEffect(filter)));
    }
}
