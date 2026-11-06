package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenCopyOfDyingSourceEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RegisterDelayedCreateTokenCopyOfDyingSourceEffect} (Ochre Jelly): registers the next end step's
 * token copy of the source, with the counter amount evaluated now against the stack entry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreateTokenCopyOfDyingSourceEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreateTokenCopyOfDyingSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayedEffect = (RegisterDelayedCreateTokenCopyOfDyingSourceEffect) effect;
        if (entry.getCard() == null) {
            return;
        }

        int counters = Math.max(0, amountEvaluationService.evaluate(gameData,
                delayedEffect.plusOnePlusOneCounters(), AmountContext.forStackEntry(entry, null)));
        CreateTokenCopyOfSourceEffect copyEffect = new CreateTokenCopyOfSourceEffect(false, 1, counters);
        gameData.queueDelayedAction(new DelayedCreateTokenCopy(
                entry.getControllerId(), copyEffect, entry.getCard()));
        log.info("Game {} - {} registers delayed token copy at next end step",
                gameData.id, entry.getCard().getName());
    }
}
