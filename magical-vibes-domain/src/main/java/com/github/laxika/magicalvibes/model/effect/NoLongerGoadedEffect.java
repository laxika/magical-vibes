package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/** Removes the goaded designation from the creatures selected by the resolving effect. */
public record NoLongerGoadedEffect() implements GoadStatusEffect {

    private static final PermanentPredicate ALL_PERMANENTS = new PermanentTruePredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return ALL_PERMANENTS;
    }

    @Override
    public boolean makesGoaded() {
        return false;
    }
}
