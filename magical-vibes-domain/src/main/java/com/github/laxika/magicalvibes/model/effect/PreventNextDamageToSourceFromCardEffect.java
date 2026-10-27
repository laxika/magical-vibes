package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Prevents the next 1 damage a specific source would deal to this ability's source permanent.
 *
 * @param damageSourceCardId the damage source's id; {@code null} in a granted ability means the
 *                           object that granted it (Torrent of Lava's "this spell", so a copy of the
 *                           spell grants a shield against the copy), bound when the ability is activated
 */
public record PreventNextDamageToSourceFromCardEffect(UUID damageSourceCardId) implements CardEffect {

    /** Prevents the next 1 damage from the object that granted this ability. */
    public PreventNextDamageToSourceFromCardEffect() {
        this(null);
    }
}
