package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class DealDamageToTargetPlayerEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return DealDamageToTargetPlayerEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        DealDamageToTargetPlayerEffect damageEffect = (DealDamageToTargetPlayerEffect) effect;
        UUID targetPlayerId = context.getEnteringControllerId();
        context.enqueue(new DealDamageToTargetPlayerEffect(damageEffect.damage()), targetPlayerId);
        String targetName = context.getGameData().playerIdToName.get(targetPlayerId);
        context.log(context.sourceCard().getName() + " triggers — deals " + damageEffect.damage()
                + " damage to " + targetName + ".");
        log.info("Game {} - {} triggers for {} entering (deal {} damage to controller)",
                context.getGameData().id, context.sourceCard().getName(),
                context.getEnteringCard().getName(), damageEffect.damage());
    }
}
