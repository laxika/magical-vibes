package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is less than or equal to the total number of counters (of
 * any type) on permanents the source's controller controls. The count is read from the battlefield
 * at evaluation time, so it tracks counters removed after a target was chosen. Used by Dimension-X
 * Pizzasaur ("destroy up to one target creature with mana value less than or equal to the number of
 * counters among permanents you control").
 */
public record PermanentManaValueAtMostControlledCountersPredicate() implements PermanentPredicate {
}
