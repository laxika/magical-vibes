package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top {@code count} cards of the controller's library. The player may put one card
 * matching {@code predicate} from among them onto the battlefield. The remaining cards go to the
 * bottom of the library in any order.
 *
 * <p>The predicate-parameterised generalisation of
 * {@link LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect}. Used by Mayael the Anima
 * ({@code count = 5}, "a creature card with power 5 or greater").
 *
 * @param count     number of cards to look at from the top of the library
 * @param predicate filter a looked-at card must satisfy to be eligible to enter the battlefield
 */
public record LookAtTopCardsPutMatchingPredicateOnBattlefieldEffect(
        int count,
        CardPredicate predicate
) implements CardEffect {
}
