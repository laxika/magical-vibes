package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Registers a delayed trigger that returns a matching card from its owner's graveyard to the
 * battlefield under the ability controller's control at the beginning of the next end step.
 *
 * <p>The card is the stack entry's triggering card ("that card").
 */
public record RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(CardPredicate filter) implements CardEffect {
}
