package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Sacrifice another creature, then reveal cards from the top of the library until a card matching
 * {@code predicate} with lesser mana value than the sacrificed creature is revealed. The matching
 * card is put onto the battlefield and the other revealed cards are put on the bottom in a random
 * order.
 *
 * <p>Wrap this effect in {@link MayEffect} for the optional "you may sacrifice" wording.
 */
public record SacrificeOtherCreatureThenRevealUntilLowerManaValueEffect(CardPredicate predicate)
        implements CardEffect {
}
