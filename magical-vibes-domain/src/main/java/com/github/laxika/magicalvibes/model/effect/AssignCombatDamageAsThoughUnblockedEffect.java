package com.github.laxika.magicalvibes.model.effect;

/**
 * Allows a blocked creature to assign its combat damage as though it weren't blocked.
 *
 * @param mandatory whether the creature must assign its combat damage that way rather than
 *                  choosing the normal blocked assignment
 */
public record AssignCombatDamageAsThoughUnblockedEffect(boolean mandatory) implements CardEffect {

    /** The optional form used by abilities such as Thorn Elemental's. */
    public AssignCombatDamageAsThoughUnblockedEffect() {
        this(false);
    }
}
