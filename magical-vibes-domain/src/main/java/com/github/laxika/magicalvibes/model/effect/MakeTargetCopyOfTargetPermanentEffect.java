package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Makes the permanent in {@code targetGroup} a permanent copy of the permanent in
 * {@code copySourceGroup}.
 * Both targets must be artifacts or creatures.
 */
public record MakeTargetCopyOfTargetPermanentEffect(int targetGroup, int copySourceGroup)
        implements CardEffect {

    public MakeTargetCopyOfTargetPermanentEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate())));
    }
}
