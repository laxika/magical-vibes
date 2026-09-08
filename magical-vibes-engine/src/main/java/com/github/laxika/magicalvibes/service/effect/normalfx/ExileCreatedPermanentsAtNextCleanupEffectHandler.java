package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtNextCleanupEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    createdId, DelayedPermanentActionKind.EXILE_TOKEN_AT_NEXT_CLEANUP));
        }
    }
}
