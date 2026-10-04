package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Reduces this spell's generic cost by {@code amount}, and optionally removes colored mana from
 * its cost, if its first target is a spell on the stack matching the predicate.
 */
public record ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryPredicate predicate, int amount,
                                                          String coloredManaReduction) implements CardEffect {
    public ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryPredicate predicate, int amount) {
        this(predicate, amount, null);
    }
}
