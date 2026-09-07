package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

/** Turns target face-down creature face up without paying a morph or disguise cost. */
public record TurnTargetFaceUpEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsFaceDownPredicate());
    }
}
