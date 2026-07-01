package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MayEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return MayEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        context.queueMay((MayEffect) effect);
        context.logTriggered();
        log.info("Game {} - {} triggers for {} entering (may effect)",
                context.getGameData().id, context.sourceCard().getName(),
                context.getEnteringCard().getName());
    }
}
