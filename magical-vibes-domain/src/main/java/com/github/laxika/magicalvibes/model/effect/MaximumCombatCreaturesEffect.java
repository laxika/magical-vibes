package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that caps the number of creatures declared as attackers and blockers in one
 * combat.
 */
public record MaximumCombatCreaturesEffect(int maxAttackers, int maxBlockers)
        implements CombatCreatureLimitEffect {

    public MaximumCombatCreaturesEffect {
        if (maxAttackers < 0 || maxBlockers < 0) {
            throw new IllegalArgumentException("Combat creature limits cannot be negative");
        }
    }
}
