package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gates an enter trigger on the entering card matching a predicate, then dispatches the
 * wrapped effect.
 */
@Slf4j
@Component
public class TriggeringCardConditionalEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return TriggeringCardConditionalEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        TriggeringCardConditionalEffect conditional = (TriggeringCardConditionalEffect) effect;
        boolean matches = context.getGameQueryService().matchesCardPredicate(
                context.getEnteringCard(), conditional.predicate(), null,
                context.getGameData(), context.getAbilityControllerId());
        if (!matches) {
            return;
        }
        context.dispatch(conditional.wrapped());
        log.info("Game {} - {} triggers for {} entering (card predicate {})",
                context.getGameData().id, context.sourceCard().getName(),
                context.getEnteringCard().getName(), conditional.predicate());
    }
}
