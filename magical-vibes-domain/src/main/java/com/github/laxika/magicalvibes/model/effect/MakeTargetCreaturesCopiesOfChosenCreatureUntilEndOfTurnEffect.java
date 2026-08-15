package com.github.laxika.magicalvibes.model.effect;

/**
 * Has any number of target creatures become copies of a creature chosen from the battlefield until
 * end of turn. The target group is supplied by the card's target declaration.
 */
public record MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
