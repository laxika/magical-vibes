package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardNameMatchesEnteringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import org.springframework.stereotype.Component;

/**
 * Gates an enter trigger on the entering permanent's name matching the source permanent's
 * imprinted card (Invader Parasite), then dispatches the wrapped effect.
 */
@Component
public class ImprintedCardNameEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return ImprintedCardNameMatchesEnteringPermanentConditionalEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        ImprintedCardNameMatchesEnteringPermanentConditionalEffect conditional =
                (ImprintedCardNameMatchesEnteringPermanentConditionalEffect) effect;
        Card imprintedCard = context.sourceCard().getImprintedCard();
        if (imprintedCard == null || !imprintedCard.getName().equals(context.getEnteringCard().getName())) {
            return;
        }
        context.dispatch(conditional.wrapped());
    }
}
