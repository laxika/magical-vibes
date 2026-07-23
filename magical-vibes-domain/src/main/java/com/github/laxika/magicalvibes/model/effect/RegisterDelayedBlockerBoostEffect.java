package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "Whenever a creature blocks this turn, it gets +power/+toughness until end of turn."
 *
 * <p>The delayed trigger fires once per unique creature declared as a blocker (not once per
 * attacker blocked). Registered by Battle Cry.
 *
 * @param power     power boost applied to each blocker
 * @param toughness toughness boost applied to each blocker
 */
public record RegisterDelayedBlockerBoostEffect(int power, int toughness) implements CardEffect {
}
