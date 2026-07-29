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
 * @param coversControlledCreatures also shield creatures the protected player controls, so the
 *                 shield is consumed by the chosen source's next damage to the player <em>or</em>
 *                 to one of their creatures (Shadowbane)
 * @param gainLifeOnlyFromBlackSource restrict {@code gainLife} to a black chosen source
 *                 (Shadowbane: "If damage from a black source is prevented this way …")
 * @param exileFromLibrary exile that many cards from the top of the protected player's library
 *                 once the shield prevents damage (Bone Mask)
 */
public record PlayerSourceNextDamageShield(UUID playerId, UUID sourceId, boolean gainLife,
                                           boolean coversControlledCreatures,
                                           boolean gainLifeOnlyFromBlackSource,
                                           boolean exileFromLibrary) {

    /** Convenience constructor for a player-only shield (Circle of Protection, Reverse Damage). */
    public PlayerSourceNextDamageShield(UUID playerId, UUID sourceId, boolean gainLife) {
        this(playerId, sourceId, gainLife, false, false, false);
    }

    /** Convenience constructor for a plain prevention shield with no life gain. */
    public PlayerSourceNextDamageShield(UUID playerId, UUID sourceId) {
        this(playerId, sourceId, false);
    }
}
