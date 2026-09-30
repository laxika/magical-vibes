package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/**
 * The controller chooses a nonbasic land type, then each matching land they control becomes a
 * copy of the targeted creature they control until end of turn and gains haste.
 */
public record EachControlledLandOfChosenNonbasicTypeBecomesCopyOfTargetCreatureUntilEndOfTurnEffect()
        implements TemporaryCopyEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
