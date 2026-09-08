package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Has any number of target creatures become copies of a creature chosen from the battlefield until
 * end of turn. The target group is supplied by the card's target declaration.
 */
public record MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect(
        PermanentPredicate chosenCreaturePredicate) implements CardEffect {

    public MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect() {
        this(new PermanentIsCreaturePredicate());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
