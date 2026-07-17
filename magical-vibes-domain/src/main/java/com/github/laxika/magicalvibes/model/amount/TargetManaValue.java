package com.github.laxika.magicalvibes.model.amount;

/**
 * The resolved target permanent's mana value at resolution time (0 when there is no legal target).
 * Used by "becomes a creature with power and toughness each equal to its mana value" animations
 * (Xenic Poltergeist). Reads the target from the stack entry the same way {@link TargetPower} does.
 */
public record TargetManaValue() implements DynamicAmount {
}
