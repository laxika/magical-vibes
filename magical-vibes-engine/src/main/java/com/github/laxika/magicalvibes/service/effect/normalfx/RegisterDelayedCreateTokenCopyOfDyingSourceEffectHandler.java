package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedCreateTokenCopy;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenCopyOfDyingSourceEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedCreateTokenCopyOfDyingSourceEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedCreateTokenCopyOfDyingSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayedEffect = (RegisterDelayedCreateTokenCopyOfDyingSourceEffect) effect;
        if (delayedEffect.initialPlusOnePlusOneCounters() <= 0 || entry.getCard() == null) {
            return;
        }

        CreateTokenCopyOfSourceEffect copyEffect = new CreateTokenCopyOfSourceEffect(
                false, 1, delayedEffect.initialPlusOnePlusOneCounters());
        gameData.queueDelayedAction(new DelayedCreateTokenCopy(
                entry.getControllerId(), copyEffect, entry.getCard()));
        log.info("Game {} - {} registers delayed token copy at next end step",
                gameData.id, entry.getCard().getName());
    }
}
