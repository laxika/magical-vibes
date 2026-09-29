package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Turns the target creature face down as a 2/2 permanent with the configured characteristics. */
public record TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter,
                                               Set<CardType> faceDownCardTypes,
                                               Set<CardSubtype> faceDownSubtypes) implements CardEffect {

    public TurnTargetCreatureFaceDownEffect() {
        this(null, Set.of(CardType.CREATURE), Set.of());
    }

    public TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter, Set<CardSubtype> faceDownSubtypes) {
        this(targetFilter, Set.of(CardType.CREATURE), faceDownSubtypes);
    }

    public TurnTargetCreatureFaceDownEffect(PermanentPredicate targetFilter) {
        this(targetFilter, Set.of(CardType.CREATURE), Set.of());
    }

    public TurnTargetCreatureFaceDownEffect(Set<CardType> faceDownCardTypes,
                                            Set<CardSubtype> faceDownSubtypes) {
        this(null, faceDownCardTypes, faceDownSubtypes);
    }

    public static TurnTargetCreatureFaceDownEffect asCyberman() {
        return new TurnTargetCreatureFaceDownEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE), Set.of(CardSubtype.CYBERMAN));
    }

    public TurnTargetCreatureFaceDownEffect {
        faceDownCardTypes = Set.copyOf(faceDownCardTypes);
        faceDownSubtypes = Set.copyOf(faceDownSubtypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.creature(), targetFilter);
    }
}
