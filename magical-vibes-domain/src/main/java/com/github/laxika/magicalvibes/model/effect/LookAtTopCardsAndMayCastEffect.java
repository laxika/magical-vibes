package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top cards of the controller's library and offers one matching spell for casting
 * without paying its mana cost. Cards not cast are put on the bottom of the library in a random
 * order.
 */
public record LookAtTopCardsAndMayCastEffect(int count, CardPredicate castPredicate)
        implements CardEffect {
}
