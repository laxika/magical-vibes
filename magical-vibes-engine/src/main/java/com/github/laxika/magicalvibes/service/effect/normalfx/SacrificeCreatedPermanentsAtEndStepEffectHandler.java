package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SacrificeCreatedPermanentsAtEndStepEffect} by queueing a
 * delayed sacrifice action for each permanent created earlier
 * in this same resolution ({@code StackEntry.createdPermanentIds}), drained by
 * {@code StepTriggerService.handleEndStepTriggers}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeCreatedPermanentsAtEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatedPermanentsAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            gameData.queueDelayedAction(new DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
                    createdId, entry.getControllerId(), Integer.MAX_VALUE));
        }
        log.info("Game {} - {} permanent(s) scheduled for sacrifice at end step by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
