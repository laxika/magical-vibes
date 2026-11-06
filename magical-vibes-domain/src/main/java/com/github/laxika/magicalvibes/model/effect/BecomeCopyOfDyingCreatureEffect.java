package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: the source permanent becomes a copy of the creature that died, except it keeps this triggered
 * ability. Used by Cemetery Puca's "you may pay {1}. If you do, this creature becomes a copy of that creature,
 * except it has this ability." death trigger.
 *
 * <p>The creature copied is the stack entry's triggering permanent as it last existed on the battlefield
 * (CR 608.2h), or its triggering card when there is no snapshot.
 */
public record BecomeCopyOfDyingCreatureEffect() implements CardEffect {
}
