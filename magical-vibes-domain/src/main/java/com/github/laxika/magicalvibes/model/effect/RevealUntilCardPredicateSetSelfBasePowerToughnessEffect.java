package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the controller's library until a card matching the predicate is revealed,
 * puts every revealed card on the bottom of that library in a random order, and sets the source
 * permanent's base power and toughness from the matching card for the controller's next turn.
 *
 * @param predicate the card that ends the reveal
 * @param multiplier the factor applied to the matching card's power and toughness
 */
public record RevealUntilCardPredicateSetSelfBasePowerToughnessEffect(
        CardPredicate predicate,
        int multiplier
) implements CardEffect {
}
