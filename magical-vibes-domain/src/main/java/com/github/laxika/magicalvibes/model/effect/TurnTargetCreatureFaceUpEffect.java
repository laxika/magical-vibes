package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

/** Turns the target face-down creature face up without paying a morph cost. */
public record TurnTargetCreatureFaceUpEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), new PermanentIsFaceDownPredicate());
    }
}
