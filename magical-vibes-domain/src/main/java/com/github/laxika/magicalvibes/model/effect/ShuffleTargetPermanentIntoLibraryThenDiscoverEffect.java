package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;

import java.util.List;

/** Shuffles the target artifact or creature into its owner's library, then that player discovers its mana value. */
public record ShuffleTargetPermanentIntoLibraryThenDiscoverEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.permanent(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        new PermanentMinManaValuePredicate(1)
                ))
        );
    }
}
