package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During resolution, exiles one matching card from the controller's graveyard and puts
 * +1/+1 counters on the source equal to half that card's mana value, rounded up.
 *
 * @param filter the card predicate for the resolution-time graveyard choice
 */
public record ExileOwnGraveyardCardPutHalfManaValueCountersOnSourceEffect(CardPredicate filter)
        implements CardEffect {
}
