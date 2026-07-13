package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the base power and toughness of every unblocked creature (any player's) to the given
 * values until end of turn (CR 613, layer 7b: counters and other modifiers still apply on top).
 * "Unblocked" is locked in at resolution time: an attacking creature that no creature is blocking.
 * Used by Inkfathom Witch ("Each unblocked creature has base power and toughness 4/1").
 */
public record SetAllUnblockedCreaturesBasePowerToughnessEffect(
        int power,
        int toughness
) implements CardEffect {
}
