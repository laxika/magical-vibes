package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Rolls one or more d6s and resolves the branch matching each result. */
public record RollD6Effect(DynamicAmount diceCount, List<CardEffect> branches, Integer trackedResult)
        implements CardEffect {

    public RollD6Effect(List<CardEffect> branches) {
        this(new Fixed(1), branches, null);
    }

    public RollD6Effect(DynamicAmount diceCount, List<CardEffect> branches) {
        this(diceCount, branches, null);
    }

    /** Rolls dice without an inherent result branch; dice-roll triggers still see each result. */
    public RollD6Effect(DynamicAmount diceCount) {
        this(diceCount, List.of(), null);
    }

    public RollD6Effect {
        branches = Collections.unmodifiableList(new ArrayList<>(branches));
        if (branches.size() != 0 && branches.size() != 6) {
            throw new IllegalArgumentException("RollD6Effect requires zero or exactly six branches");
        }
        if (trackedResult != null && (trackedResult < 1 || trackedResult > 6)) {
            throw new IllegalArgumentException("RollD6Effect tracked result must be between 1 and 6");
        }
    }

    /** Tracks how many dice produce {@code result} for a following event-value effect. */
    public RollD6Effect withTrackedResult(int result) {
        return new RollD6Effect(diceCount, branches, result);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect branch : branches) {
            if (branch == null) {
                continue;
            }
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
