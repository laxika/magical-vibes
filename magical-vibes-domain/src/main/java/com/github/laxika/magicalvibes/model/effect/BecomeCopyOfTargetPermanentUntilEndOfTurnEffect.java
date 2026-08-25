package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Makes the first target permanent become a copy of the second target permanent until end of turn.
 */
public record BecomeCopyOfTargetPermanentUntilEndOfTurnEffect(Set<CardType> additionalTypes)
        implements CardEffect {

    public BecomeCopyOfTargetPermanentUntilEndOfTurnEffect {
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
