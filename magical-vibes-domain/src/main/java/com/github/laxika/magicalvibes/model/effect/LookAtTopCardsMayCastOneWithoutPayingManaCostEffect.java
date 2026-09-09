package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Looks at cards from the top of the controller's library and may cast one eligible nonland card
 * without paying its mana cost. Cards not cast are put on the bottom in a random order.
 *
 * @param lookCount number of cards to look at, capped by the library size
 * @param maxManaValue maximum mana value of the spell that may be cast
 */
public record LookAtTopCardsMayCastOneWithoutPayingManaCostEffect(
        DynamicAmount lookCount,
        DynamicAmount maxManaValue
) implements CardEffect {
}
