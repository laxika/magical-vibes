package com.github.laxika.magicalvibes.service.battlefield.entertrigger.handler;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerContext;
import com.github.laxika.magicalvibes.service.battlefield.entertrigger.EnterTriggerHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gates an enter trigger on at least {@code minCount} permanents matching a predicate having
 * entered under the affected player's control this turn, then dispatches the wrapped effect.
 */
@Component
public class PermanentEnteredThisTurnConditionalEnterTriggerHandler implements EnterTriggerHandler {

    @Override
    public Class<? extends CardEffect> handledType() {
        return PermanentEnteredThisTurnConditionalEffect.class;
    }

    @Override
    public void handle(EnterTriggerContext context, CardEffect effect) {
        PermanentEnteredThisTurnConditionalEffect conditional = (PermanentEnteredThisTurnConditionalEffect) effect;
        List<Card> entered = context.getGameData().permanentsEnteredBattlefieldThisTurn
                .getOrDefault(context.getDefaultTargetPlayerId(), List.of());
        long matchCount = entered.stream()
                .filter(c -> context.getGameQueryService().matchesCardPredicate(c, conditional.predicate(), null))
                .count();
        if (matchCount < conditional.minCount()) {
            return;
        }
        context.dispatch(conditional.wrapped());
    }
}
