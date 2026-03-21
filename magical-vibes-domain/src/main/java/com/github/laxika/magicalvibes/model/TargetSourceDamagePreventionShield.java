package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage prevention shield that prevents damage from a specific source to a specific target.
 * When the chosen source would deal damage to the chosen target, the shield prevents up to
 * {@code remainingAmount} damage. Used by Healing Grace.
 *
 * @param targetId        the protected target (player UUID or permanent UUID)
 * @param sourceId        the chosen source permanent
 * @param remainingAmount how much prevention remains on this shield
 */
public record TargetSourceDamagePreventionShield(
        UUID targetId,
        UUID sourceId,
        int remainingAmount
) {
    /**
     * Returns a new shield with the remaining amount reduced by the given consumed amount.
     */
    public TargetSourceDamagePreventionShield withReducedAmount(int consumed) {
        return new TargetSourceDamagePreventionShield(targetId, sourceId, remainingAmount - consumed);
    }
}
