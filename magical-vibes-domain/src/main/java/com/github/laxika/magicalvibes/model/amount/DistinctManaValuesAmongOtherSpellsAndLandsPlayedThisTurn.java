package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of different mana values among the other spells the controller cast and the lands they played this turn
 * (coststorm: "copy it for each different mana value among other spells and lands you've played this turn"). A spell's
 * mana value is the one it had on the stack, its X included. "Other" leaves out the stack entry's triggering spell, or
 * with none the resolving entry's card.
 */
public record DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn() implements DynamicAmount {
}
