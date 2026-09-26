package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Rolls a dynamic number of dice and resolves one branch for each odd or even result. */
public record RollDiceEffect(DynamicAmount diceCount, int sides,
                             CardEffect oddResult, CardEffect evenResult) implements CardEffect {

    public RollDiceEffect {
        if (diceCount == null) {
            throw new IllegalArgumentException("RollDiceEffect requires a dice count");
        }
        if (sides < 2) {
            throw new IllegalArgumentException("RollDiceEffect requires at least two sides");
        }
    }

    public RollDiceEffect(int diceCount, int sides, CardEffect oddResult, CardEffect evenResult) {
        this(new Fixed(diceCount), sides, oddResult, evenResult);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect branch : new CardEffect[]{oddResult, evenResult}) {
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
