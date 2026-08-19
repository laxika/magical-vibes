package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.UUID;

/**
 * Static effect that caps the number of creatures declared as attackers and blockers in one
 * combat.
 */
public record MaximumCombatCreaturesEffect(int maxAttackers, int maxBlockers,
                                           CombatAttackTargetScope attackTargetScope)
        implements CombatCreatureLimitEffect {

    public MaximumCombatCreaturesEffect(int maxAttackers, int maxBlockers) {
        this(maxAttackers, maxBlockers, CombatAttackTargetScope.ALL);
    }

    public MaximumCombatCreaturesEffect {
        if (maxAttackers < 0 || maxBlockers < 0) {
            throw new IllegalArgumentException("Combat creature limits cannot be negative");
        }
        Objects.requireNonNull(attackTargetScope, "attackTargetScope");
    }

    @Override
    public boolean appliesToAttackTarget(UUID sourceControllerId, UUID attackTargetId) {
        return appliesToAttackTarget(sourceControllerId, null, attackTargetId);
    }

    @Override
    public boolean appliesToAttackTarget(UUID sourceControllerId, UUID sourcePermanentId,
                                         UUID attackTargetId) {
        return switch (attackTargetScope) {
            case ALL -> true;
            case CONTROLLER -> Objects.equals(sourceControllerId, attackTargetId);
            case SOURCE_PERMANENT -> Objects.equals(sourcePermanentId, attackTargetId);
        };
    }
}
