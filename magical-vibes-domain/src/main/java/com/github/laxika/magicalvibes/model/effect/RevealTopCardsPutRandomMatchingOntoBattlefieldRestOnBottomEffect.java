package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top {@code count} cards of the controller's library, puts one matching card chosen
 * at random onto the battlefield, and puts all other revealed cards on the bottom of the library
 * in a random order.
 *
 * @param count the maximum number of cards to reveal
 * @param predicate the cards eligible for the random battlefield placement
 */
public record RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect(
        int count,
        CardPredicate predicate
) implements CardEffect {
}
