package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Resolution-time optional exile from the controller's graveyard followed by life loss for each
 * opponent when a card was actually exiled.
 */
public record ExileOwnGraveyardCardThenEachOpponentLosesLifeEffect(CardPredicate filter, int lifeLoss)
        implements CardEffect {
}
