package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the controller's library until the required number of cards matching the
 * predicate have been revealed, or the library is empty. By default, the controller may put any
 * number of the matching revealed cards onto the battlefield, then the remaining revealed cards go
 * to the bottom of the library in a random order. The all-matching variant puts every matching
 * revealed card onto the battlefield tapped without a choice.
 *
 * @param requiredCount the number of matching cards that ends the reveal
 * @param predicate the cards that count toward the required number and may be put onto the battlefield
 * @param enterTapped whether cards put onto the battlefield enter tapped
 * @param putAllMatching whether every matching revealed card must be put onto the battlefield
 */
public record RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
        DynamicAmount requiredCount,
        CardPredicate predicate,
        boolean enterTapped,
        boolean putAllMatching
) implements CardEffect {

    public RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
            DynamicAmount requiredCount, CardPredicate predicate) {
        this(requiredCount, predicate, false, false);
    }

    public static RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect
    allMatchingOntoBattlefieldTapped(DynamicAmount requiredCount, CardPredicate predicate) {
        return new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                requiredCount, predicate, true, true);
    }
}
