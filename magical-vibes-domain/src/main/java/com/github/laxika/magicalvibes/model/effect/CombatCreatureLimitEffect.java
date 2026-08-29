package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability for a static effect that limits the number of creatures declared as attackers or
 * blockers in one combat.
 */
public interface CombatCreatureLimitEffect extends CardEffect {

    /** Maximum number of creatures that may be declared as attackers. */
    int maxAttackers();

    /** Maximum number of distinct creatures that may be declared as blockers. */
    int maxBlockers();

    /**
     * Whether an attacking creature declared against the given target is counted by this effect.
     * The source controller is supplied by the battlefield that carries the static effect.
     */
    default boolean appliesToAttackTarget(UUID sourceControllerId, UUID attackTargetId) {
        return true;
    }

    /**
     * Whether an attacker declared against {@code attackTargetId} is counted when the limiting
     * effect is carried by {@code sourcePermanentId}.
     */
    default boolean appliesToAttackTarget(UUID sourceControllerId, UUID sourcePermanentId,
                                          UUID attackTargetId) {
        return appliesToAttackTarget(sourceControllerId, attackTargetId);
    }
}
