package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: the source permanent becomes a copy of the stack entry's triggering permanent, the creature
 * that entered, except it keeps this triggered ability. Used by Unstable Shapeshifter's "Whenever another creature
 * enters, this creature becomes a copy of that creature, except it has this ability." The creature is read as the
 * ability resolves: as it is while it is on the battlefield, else as it last existed there.
 */
public record BecomeCopyOfEnteringCreatureEffect() implements CardEffect {
}
