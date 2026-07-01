package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GainLifeEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return GainLifeEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        GainLifeEffect gainLife = (GainLifeEffect) effect;
        context.enqueue(new GainLifeEffect(gainLife.amount()));
        String controllerName = context.getGameData().playerIdToName.get(context.getAbilityControllerId());
        context.log(context.sourceCard().getName() + " triggers — " + controllerName
                + " will gain " + gainLife.amount() + " life.");
        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                context.getGameData().id, context.sourceCard().getName(),
                context.getEnteringCard().getName(), gainLife.amount());
    }
}
