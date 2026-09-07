package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token whose base power and toughness equal the total power of the nontoken creatures
 * that died in the triggering simultaneous death event.
 *
 * @param tokenTemplate token characteristics to use, with power and toughness replaced when the
 *                      trigger is collected
 */
public record CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
