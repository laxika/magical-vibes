package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * revealed. That card is put onto the battlefield, and the other revealed cards are put on the
 * bottom of the library in a random order.
 */
public record RevealUntilCardPredicateToBattlefieldRestOnBottomRandomEffect(
        CardPredicate predicate
) implements CardEffect {
}
