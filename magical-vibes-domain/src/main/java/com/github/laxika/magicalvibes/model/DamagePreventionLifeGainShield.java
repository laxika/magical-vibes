package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A target-specific prevention shield that gains life for its controller when it prevents damage.
 * The protected target may be a player or permanent, while the life-gain player is fixed when the
 * spell resolves.
 */
public record DamagePreventionLifeGainShield(
        UUID targetId,
        UUID lifeGainPlayerId,
        int remainingAmount
) {

    /** Returns a copy with the given amount consumed. */
    public DamagePreventionLifeGainShield withReducedAmount(int consumed) {
        return new DamagePreventionLifeGainShield(targetId, lifeGainPlayerId, remainingAmount - consumed);
    }
}
