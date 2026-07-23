package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentIfDidNotAttackAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetIfDidNotAttackAtEndStepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Schedules the target for destruction at the next end step if it did not attack this turn
 * ({@link DestroyPermanentIfDidNotAttackAtEndStep}, drained in {@code StepTriggerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyTargetIfDidNotAttackAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetIfDidNotAttackAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        gameData.queueDelayedAction(new DestroyPermanentIfDidNotAttackAtEndStep(target.getId()));

        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(target.getCard(),
                        " will be destroyed at the beginning of the next end step if it doesn't attack this turn."));
        log.info("Game {} - {} scheduled for conditional end-step destruction if it doesn't attack",
                gameData.id, target.getCard().getName());
    }
}
