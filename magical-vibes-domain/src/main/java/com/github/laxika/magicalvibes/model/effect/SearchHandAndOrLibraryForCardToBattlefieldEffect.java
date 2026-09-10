package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's hand and/or library for one card matching a predicate and puts it
 * onto the battlefield. The controller chooses between matching cards in the two zones, and a
 * library search shuffles the library afterward.
 */
public record SearchHandAndOrLibraryForCardToBattlefieldEffect(CardPredicate filter)
        implements CardEffect {
}
