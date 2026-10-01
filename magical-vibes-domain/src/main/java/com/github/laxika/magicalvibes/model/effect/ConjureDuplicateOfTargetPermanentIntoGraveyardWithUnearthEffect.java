package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Conjures a duplicate of a target controlled nontoken creature or artifact into its controller's graveyard. */
public record ConjureDuplicateOfTargetPermanentIntoGraveyardWithUnearthEffect() implements CardEffect {

    private static final PermanentPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsArtifactPredicate())),
            new PermanentNotPredicate(new PermanentIsTokenPredicate()),
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), TARGET);
    }
}
