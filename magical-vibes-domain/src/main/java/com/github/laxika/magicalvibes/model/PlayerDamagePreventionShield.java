package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * "Prevent the next N damage that would be dealt to you / to target player" (CR 615.7): the part of the shield that
 * is left. The floating effect carrying it names the protected player, or none for the shield any damage may use
 * (Samite Healer's mirror, tests); using part of it replaces it with
 * {@link #reducedBy(int)}, using all of it ends it.
 *
 * @param amount     the damage the shield can still prevent
 * @param combatOnly whether it prevents combat damage only
 */
public record PlayerDamagePreventionShield(int amount, boolean combatOnly) implements CardEffect {

    /** The same shield after {@code prevented} damage has been prevented. */
    public PlayerDamagePreventionShield reducedBy(int prevented) {
        return new PlayerDamagePreventionShield(amount - prevented, combatOnly);
    }
}
