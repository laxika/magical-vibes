package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Targeted indefinite effect that replaces a permanent's creature subtypes. */
public record TargetPermanentBecomesSubtypeEffect(CardSubtype subtype) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
