package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GainLifeEqualToToughnessEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return GainLifeEqualToToughnessEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        int toughness = context.getEnteringCard().getToughness();
        context.enqueue(new GainLifeEffect(toughness));
        String controllerName = context.getGameData().playerIdToName.get(context.getAbilityControllerId());
        context.log(context.sourceCard().getName() + " triggers — " + controllerName
                + " will gain " + toughness + " life.");
        log.info("Game {} - {} triggers for {} entering (toughness={})",
                context.getGameData().id, context.sourceCard().getName(),
                context.getEnteringCard().getName(), toughness);
    }
}
