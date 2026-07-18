package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ExileSelfAtEndStepEffect} from an activated ability by scheduling the source
 * permanent for exile at the beginning of the next end step (an {@link DelayedPermanentAction}
 * delayed action, drained by {@code StepTriggerService.handleEndStepTriggers}). Used by Dark Maze.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfAtEndStepEffect.class;
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

        gameData.queueDelayedAction(new DelayedPermanentAction(sourceId, DelayedPermanentActionKind.EXILE_AT_END_STEP));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(), " will be exiled at the beginning of the next end step."));
        log.info("Game {} - {} scheduled for exile at end step", gameData.id, source.getCard().getName());
    }
}
