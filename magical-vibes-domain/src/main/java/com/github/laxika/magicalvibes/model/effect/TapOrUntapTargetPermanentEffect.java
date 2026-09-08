package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record TapOrUntapTargetPermanentEffect(PermanentPredicate targetPredicate) implements CardEffect {

    public TapOrUntapTargetPermanentEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }
}
