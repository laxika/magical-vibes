package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/** Redirects all damage that would be dealt this turn to the controller and creatures they control to the target creature. */
public record RedirectYourDamageToTargetCreatureThisTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
