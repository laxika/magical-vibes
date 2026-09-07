package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the controller's library until the required number of matching cards have
 * been revealed, or the library is empty. All matching revealed cards go to the controller's
 * graveyard, and all other revealed cards go to the bottom of the library in a random order.
 */
public record RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect(
        DynamicAmount requiredCount,
        CardPredicate predicate
) implements CardEffect {
}
