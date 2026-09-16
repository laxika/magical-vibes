package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Rolls a d6 and resolves the branch matching the result. */
public record RollD6Effect(List<CardEffect> branches) implements CardEffect {

    public RollD6Effect {
        branches = List.copyOf(branches);
        if (branches.size() != 6) {
            throw new IllegalArgumentException("RollD6Effect requires exactly six branches");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect branch : branches) {
            TargetSpec branchSpec = branch.targetSpec();
            if (branchSpec.declaredTarget() != null) {
                return branchSpec;
            }
            if (branchSpec.selfTargeting()) {
                implicitSourceSpec = branchSpec;
            }
        }
        return implicitSourceSpec;
    }
}
