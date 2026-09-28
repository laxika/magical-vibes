package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/** Conjures a fresh-identity duplicate of each targeted nontoken permanent into the controller's hand. */
public record ConjureDuplicatesOfTargetPermanentsIntoHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.permanent(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()))));
    }
}
