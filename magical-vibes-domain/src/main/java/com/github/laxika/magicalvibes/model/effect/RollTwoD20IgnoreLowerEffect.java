package com.github.laxika.magicalvibes.model.effect;

/** Rolls two d20, keeps the higher result, and resolves one of two result branches. */
public record RollTwoD20IgnoreLowerEffect(CardEffect lowBranch, CardEffect highBranch,
                                          int lowBranchMax) implements CardEffect {

    public RollTwoD20IgnoreLowerEffect {
        if (lowBranch == null || highBranch == null) {
            throw new IllegalArgumentException("Both two-d20 branches are required");
        }
        if (lowBranchMax < 1 || lowBranchMax > 19) {
            throw new IllegalArgumentException("The low two-d20 branch must end between 1 and 19");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec lowSpec = lowBranch.targetSpec();
        if (lowSpec.declaredTarget() != null || lowSpec.selfTargeting()) {
            return lowSpec;
        }
        return highBranch.targetSpec();
    }
}
