package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Searches the controller's library of the target permanent for a card with the same name and
 * puts it onto the battlefield under this effect's controller's control, then shuffles.
 */
public record SearchTargetPermanentControllerLibraryForSameNameToBattlefieldEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()));
    }
}
