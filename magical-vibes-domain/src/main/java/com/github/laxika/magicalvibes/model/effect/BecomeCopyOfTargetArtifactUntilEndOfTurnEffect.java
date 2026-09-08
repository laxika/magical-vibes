package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/** Causes the source permanent to become a copy of the target artifact until end of turn. */
public record BecomeCopyOfTargetArtifactUntilEndOfTurnEffect(boolean retainsAbility) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }
}
