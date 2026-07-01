package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterCreatureConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gates a power-/stat-based enter trigger (e.g. Garruk's Packleader) whose condition is
 * computable from the entering creature alone, then dispatches the wrapped effect.
 */
@Slf4j
@Component
public class EnterCreatureConditionalEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return EnterCreatureConditionalEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        EnterCreatureConditionalEffect conditional = (EnterCreatureConditionalEffect) effect;
        Card enteringCreature = context.getEnteringCard();
        if (!conditional.testEnteringCreature(enteringCreature)) {
            return;
        }
        context.dispatch(conditional.wrapped());
        log.info("Game {} - {} triggers for {} entering ({})",
                context.getGameData().id, context.sourceCard().getName(),
                enteringCreature.getName(), conditional.triggerDescription(enteringCreature));
    }
}
