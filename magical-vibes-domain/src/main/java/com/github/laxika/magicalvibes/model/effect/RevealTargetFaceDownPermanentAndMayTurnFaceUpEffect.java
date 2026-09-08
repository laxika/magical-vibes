package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

/** Reveals a target face-down permanent and may turn it face up if its card is a creature. */
public record RevealTargetFaceDownPermanentAndMayTurnFaceUpEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentIsFaceDownPredicate());
    }
}
