package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

/** The controller looks at the card represented by target face-down creature. */
public record LookAtFaceDownCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsFaceDownPredicate());
    }
}
