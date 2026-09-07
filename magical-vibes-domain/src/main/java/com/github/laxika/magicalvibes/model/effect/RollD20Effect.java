package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Rolls a d20 and resolves the branch matching the result range. */
public record RollD20Effect(CardEffect zeroOrLess, CardEffect oneToNine, CardEffect tenToNineteen,
                            CardEffect twenty, DynamicAmount amountToSubtract,
                            int firstBranchMax, boolean repeatOnHighBranch,
                            boolean twentyUsesRawResult)
        implements CardEffect {

    public RollD20Effect {
        if (firstBranchMax < 1 || firstBranchMax > 19) {
            throw new IllegalArgumentException("The first d20 branch must end between 1 and 19");
        }
    }

    /** Creates the standard 1-9, 10-19, and 20 ranges. */
    public RollD20Effect(CardEffect oneToNine, CardEffect tenToNineteen, CardEffect twenty) {
        this(null, oneToNine, tenToNineteen, twenty, null, 9, false, false);
    }

    /** Creates four branches for results 1, 2-9, 10-19, and 20. */
    public RollD20Effect(CardEffect one, CardEffect twoToNine, CardEffect tenToNineteen,
                         CardEffect twenty) {
        this(one, twoToNine, tenToNineteen, twenty, new Fixed(1), 8, false, true);
    }

    /** Creates a two-outcome roll with the second branch covering results 10 through 20. */
    public RollD20Effect(CardEffect oneToNine, CardEffect tenToTwenty) {
        this(null, oneToNine, tenToTwenty, null, null, 9, false, false);
    }

    /** Creates a roll whose high branch may be followed by another roll. */
    public static RollD20Effect withRepeatOnHighBranch(CardEffect lowBranch, CardEffect highBranch,
                                                        int lowBranchMax) {
        return new RollD20Effect(null, lowBranch, highBranch, null, null, lowBranchMax, true, false);
    }

    /** Creates a roll whose result is reduced by an amount before branch selection. */
    public static RollD20Effect withSubtractedAmount(DynamicAmount amountToSubtract,
                                                       CardEffect zeroOrLess, CardEffect oneToNine,
                                                       CardEffect tenToNineteen, CardEffect twenty) {
        return new RollD20Effect(zeroOrLess, oneToNine, tenToNineteen, twenty,
                amountToSubtract, 9, false, false);
    }

    /** Backward-compatible full constructor with ordinary natural-20 branch semantics. */
    public RollD20Effect(CardEffect zeroOrLess, CardEffect oneToNine, CardEffect tenToNineteen,
                         CardEffect twenty, DynamicAmount amountToSubtract, int firstBranchMax,
                         boolean repeatOnHighBranch) {
        this(zeroOrLess, oneToNine, tenToNineteen, twenty, amountToSubtract, firstBranchMax,
                repeatOnHighBranch, false);
    }

    public RollD20Effect copyForRepeat() {
        return new RollD20Effect(zeroOrLess, oneToNine, tenToNineteen, twenty,
                amountToSubtract, firstBranchMax, repeatOnHighBranch, twentyUsesRawResult);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec implicitSourceSpec = TargetSpec.NONE;
        for (CardEffect branch : new CardEffect[]{zeroOrLess, oneToNine, tenToNineteen, twenty}) {
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
