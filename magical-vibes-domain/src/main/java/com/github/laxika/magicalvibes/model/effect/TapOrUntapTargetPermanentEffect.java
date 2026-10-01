package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record TapOrUntapTargetPermanentEffect(PermanentPredicate targetPredicate, boolean chooseAction) implements CardEffect {
    public TapOrUntapTargetPermanentEffect(PermanentPredicate targetPredicate) {
        this(targetPredicate, false);
    }

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
