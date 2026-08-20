package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers the controller up to one matching card from each graveyard and exiles the chosen cards
 * tracked with the resolving ability's source permanent.
 */
public record ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect(CardPredicate filter)
        implements CardEffect {
}
