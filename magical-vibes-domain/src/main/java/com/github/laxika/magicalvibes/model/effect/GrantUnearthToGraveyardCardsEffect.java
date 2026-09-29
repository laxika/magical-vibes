package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants unearth with a fixed cost to matching cards in the controller's graveyard. In a normal
 * effect slot the grant lasts until end of turn; in a static slot it is not currently used.
 */
public record GrantUnearthToGraveyardCardsEffect(CardPredicate filter, String unearthCost)
        implements CardEffect {
}
