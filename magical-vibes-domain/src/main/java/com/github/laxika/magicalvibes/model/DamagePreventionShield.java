package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * "Prevent the next N damage that would be dealt to …" (CR 615.7): the part of the shield that is left. The floating
 * effect carrying it names the protected player or permanent, or neither for the shield any damage may use (Samite
 * Healer's mirror, tests); using part of it replaces it with {@link #reducedBy(int)}, using all of it ends it.
 *
 * @param amount             the damage the shield can still prevent
 * @param combatOnly         whether it prevents combat damage only
 * @param plusOneCounters    whether a +1/+1 counter is put on the protected permanent for each 1 damage it prevents
 *                           (Temper)
 */
public record DamagePreventionShield(int amount, boolean combatOnly, boolean plusOneCounters) implements CardEffect {

    /** A shield that prevents damage and does nothing else. */
    public DamagePreventionShield(int amount, boolean combatOnly) {
        this(amount, combatOnly, false);
    }

    /** The same shield after {@code prevented} damage has been prevented. */
    public DamagePreventionShield reducedBy(int prevented) {
        return new DamagePreventionShield(amount - prevented, combatOnly, plusOneCounters);
    }
}
