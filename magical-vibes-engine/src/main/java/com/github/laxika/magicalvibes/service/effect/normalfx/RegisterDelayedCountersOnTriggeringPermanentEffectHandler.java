package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCountersOnTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link RegisterDelayedCountersOnTriggeringPermanentEffect}: queues a
 * {@link PutCounterOnPermanentAtNextEndStep} delayed action for the stack entry's triggering
 * permanent, drained by {@code StepTriggerService} at the beginning of the next end step.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCountersOnTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCountersOnTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID permanentId = entry.getTriggeringPermanentId();
        if (permanentId == null) {
            return;
        }
        var registered = (RegisterDelayedCountersOnTriggeringPermanentEffect) effect;
        gameData.queueDelayedAction(new PutCounterOnPermanentAtNextEndStep(
                entry.getCard(), entry.getControllerId(), permanentId, registered.counters()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " will put counters on the creature at the beginning of the next end step."));
        log.info("Game {} - {} schedules counters for triggering permanent at the next end step",
                gameData.id, entry.getCard().getName());
    }
}
