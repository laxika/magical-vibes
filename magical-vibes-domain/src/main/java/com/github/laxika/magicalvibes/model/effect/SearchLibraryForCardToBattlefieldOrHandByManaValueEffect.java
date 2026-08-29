package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's library for one card matching {@code filter}, then puts the chosen
 * card onto the battlefield when its mana value is at most {@code maxManaValue}; otherwise it is
 * put into the controller's hand.
 */
public record SearchLibraryForCardToBattlefieldOrHandByManaValueEffect(CardPredicate filter,
                                                                        int maxManaValue)
        implements CardEffect {
}
