package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Lets the controller exile a matching card from hand and create a token copy of that card. */
public record ExileCardFromHandAndCreateTokenCopyEffect(
        CardPredicate filter,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect {
}
