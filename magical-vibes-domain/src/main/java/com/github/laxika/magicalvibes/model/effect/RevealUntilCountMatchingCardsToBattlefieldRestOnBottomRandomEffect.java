package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the controller's library until the required number of cards matching the
 * predicate have been revealed, or the library is empty. The controller may put any number of the
 * matching revealed cards onto the battlefield, then the remaining revealed cards go to the bottom
 * of the library in a random order.
 *
 * @param requiredCount the number of matching cards that ends the reveal
 * @param predicate the cards that count toward the required number and may be put onto the battlefield
 */
public record RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
        DynamicAmount requiredCount,
        CardPredicate predicate
) implements CardEffect {
}
