package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A one-shot damage prevention shield: the next time the chosen source would deal damage to the
 * given player this turn, that entire damage event is prevented and the shield is consumed
 * (Circle of Protection cycle). Distinct from the whole-turn {@code playerSourceDamagePreventionIds}
 * shield, which keeps preventing every subsequent event from the source.
 *
 * <p>When {@code gainLife} is true, the protected player also gains life equal to the damage
 * prevented this way (Reverse Damage).
 *
 * @param playerId the protected player
 * @param sourceId the chosen source permanent
 * @param gainLife whether the protected player gains life equal to the prevented damage
 */
public record PlayerSourceNextDamageShield(UUID playerId, UUID sourceId, boolean gainLife) {

    /** Convenience constructor for a plain prevention shield with no life gain. */
    public PlayerSourceNextDamageShield(UUID playerId, UUID sourceId) {
        this(playerId, sourceId, false);
    }
}
