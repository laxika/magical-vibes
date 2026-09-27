package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/** Conjures a fresh-identity duplicate of each targeted nontoken creature into the controller's hand. */
public record ConjureDuplicateOfTargetCreatureIntoHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentControlledBySourceControllerPredicate())));
    }
}
