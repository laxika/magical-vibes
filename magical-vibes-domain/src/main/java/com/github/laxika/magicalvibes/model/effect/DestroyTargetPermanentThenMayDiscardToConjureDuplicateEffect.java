package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/** Destroys a target nonland, nontoken permanent an opponent controls, then may discard to conjure its duplicate. */
public record DestroyTargetPermanentThenMayDiscardToConjureDuplicateEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
