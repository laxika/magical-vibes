package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtControllerEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtControllerEndStepEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ExileCreatedPermanentsAtControllerEndStepEffect} by recording the controller of
 * the resolving entry, so the delayed action skips the opponent's end step.
 */
@Component
@Slf4j
public class ExileCreatedPermanentsAtControllerEndStepEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCreatedPermanentsAtControllerEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            gameData.queueDelayedAction(new ExilePermanentAtControllerEndStep(createdId, controllerId));
        }
        log.info("Game {} - {} permanent(s) scheduled for exile at {}'s next end step",
                gameData.id, entry.getCreatedPermanentIds().size(),
                gameData.playerIdToName.get(controllerId));
    }
}
