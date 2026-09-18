package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Makes the targeted creature the controller's only commander and resets its commander tax. */
public record MakeTargetCreatureCommanderEffect() implements CardEffect {

    private static final PermanentPredicate OWNED_AND_CONTROLLED_CREATURE = new PermanentAllOfPredicate(
            List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentOwnedBySourceControllerPredicate()));

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), OWNED_AND_CONTROLLED_CREATURE);
    }
}
