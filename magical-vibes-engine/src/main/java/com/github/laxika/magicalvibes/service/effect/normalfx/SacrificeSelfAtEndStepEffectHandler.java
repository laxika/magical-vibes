package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SacrificeSelfAtEndStepEffect} by scheduling the source permanent for sacrifice at
 * the beginning of the next end step (a {@link DelayedPermanentAction} with kind
 * {@link DelayedPermanentActionKind#SACRIFICE_AT_END_STEP}, drained by
 * {@code StepTriggerService.handleEndStepTriggers}). Used by Brackwater Elemental's attack/block
 * trigger.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeSelfAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        gameData.queueDelayedAction(new DelayedPermanentAction(sourceId, DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));

        String logEntry = source.getCard().getName() + " will be sacrificed at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} scheduled for sacrifice at end step", gameData.id, source.getCard().getName());
    }
}
