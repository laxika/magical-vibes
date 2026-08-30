package com.github.laxika.magicalvibes.model.effect;

/**
 * This creature can attack as though it didn't have defender. In a static slot
 * it is a continuous permission; in a resolving ability it grants the source
 * permanent that permission until end of turn.
 */
public record CanAttackAsThoughNoDefenderEffect(boolean targeted) implements NoDefenderAttackPermissionEffect {

    public CanAttackAsThoughNoDefenderEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }

    @Override
    public boolean grantsCarrierAttackAsThoughNoDefender() {
        return !targeted;
    }
}
