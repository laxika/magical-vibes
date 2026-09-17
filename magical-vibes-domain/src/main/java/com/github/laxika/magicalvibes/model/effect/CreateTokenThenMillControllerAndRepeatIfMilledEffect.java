package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a token, mills the controller, and repeats after a matching card is milled. */
public record CreateTokenThenMillControllerAndRepeatIfMilledEffect(
        CreateTokenEffect tokenEffect,
        CardPredicate repeatPredicate
) implements CardEffect {
}
