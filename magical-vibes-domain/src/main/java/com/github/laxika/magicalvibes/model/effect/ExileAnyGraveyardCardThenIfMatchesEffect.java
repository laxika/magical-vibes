package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At resolution, optionally exiles one matching card from any graveyard. If a card was exiled
 * and it matches the second predicate, the follow-up effect is resolved immediately afterward.
 */
public record ExileAnyGraveyardCardThenIfMatchesEffect(
        CardPredicate exileFilter,
        CardPredicate thenFilter,
        CardEffect thenEffect
) implements CardEffect {
}
