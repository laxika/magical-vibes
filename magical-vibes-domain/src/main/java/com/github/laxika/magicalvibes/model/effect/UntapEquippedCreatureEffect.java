package com.github.laxika.magicalvibes.model.effect;

/**
 * Untaps the source equipment's equipped creature. If the source equipment is not attached to a
 * creature, the effect does nothing.
 *
 * <p>Used to model equipment-granted triggered abilities such as Thornbite Staff's "Whenever a
 * creature dies, untap this creature" (placed in the {@code ON_ANY_CREATURE_DIES} slot on the
 * equipment).
 */
public record UntapEquippedCreatureEffect() implements CardEffect {
}
