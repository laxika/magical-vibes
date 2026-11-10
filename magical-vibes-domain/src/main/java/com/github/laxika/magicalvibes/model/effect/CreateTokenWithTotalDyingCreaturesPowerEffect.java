package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token whose base power and toughness equal the total power of the creatures a "whenever one or more
 * creatures die" ability triggered for, as they last existed on the battlefield (The Skullspore Nexus): the stack
 * entry's event value, which the "one or more" collector records.
 *
 * @param tokenTemplate token characteristics to use, with power and toughness replaced as the ability resolves
 */
public record CreateTokenWithTotalDyingCreaturesPowerEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
