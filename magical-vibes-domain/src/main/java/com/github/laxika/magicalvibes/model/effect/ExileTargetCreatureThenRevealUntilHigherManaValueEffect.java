package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/**
 * Exiles a target creature, then reveals its controller's library until a creature card with a
 * higher mana value is found and puts that card onto the battlefield.
 */
public record ExileTargetCreatureThenRevealUntilHigherManaValueEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
