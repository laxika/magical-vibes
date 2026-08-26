package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the active player choose a matching card from their hand and put it onto the battlefield.
 * The active player is taken from the resolving stack entry, falling back to its controller when
 * no active player was captured.
 *
 * @param predicate filter for eligible cards in the active player's hand
 * @param label human-readable description of the eligible card type
 */
public record ActivePlayerPutsCardFromHandOntoBattlefieldEffect(CardPredicate predicate, String label)
        implements CardEffect {
}
