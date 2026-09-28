package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Turns the target creature face down as a 2/2 creature. */
public record TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter,
                                               Set<CardSubtype> faceDownSubtypes) implements CardEffect {

    public TurnTargetCreatureFaceDownEffect() {
        this(null, Set.of());
    }

    public TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter) {
        this(targetFilter, Set.of());
    }

    public TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter, Set<CardSubtype> faceDownSubtypes) {
        this.targetFilter = targetFilter;
        this.faceDownSubtypes = faceDownSubtypes == null ? Set.of() : Set.copyOf(faceDownSubtypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.creature(), targetFilter);
    }
}
