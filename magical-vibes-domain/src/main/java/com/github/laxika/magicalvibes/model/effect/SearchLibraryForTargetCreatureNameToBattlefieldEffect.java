package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Searches the controller's library for a card with the same name as the target nontoken creature
 * and puts it onto the battlefield, then shuffles.
 */
public record SearchLibraryForTargetCreatureNameToBattlefieldEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate()));
    }
}
