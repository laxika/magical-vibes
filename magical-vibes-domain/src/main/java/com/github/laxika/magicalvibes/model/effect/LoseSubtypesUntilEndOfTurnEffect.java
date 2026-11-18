package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * One-shot self effect: the source permanent loses the given subtypes until end of turn
 * (e.g. Haunted Plate Mail "…that's no longer an Equipment"). Registered as a floating layer-4 effect
 * on the source (the record itself is the payload), applied at its own timestamp.
 */
public record LoseSubtypesUntilEndOfTurnEffect(Set<CardSubtype> subtypes) implements CardEffect {

    public LoseSubtypesUntilEndOfTurnEffect {
        subtypes = Set.copyOf(subtypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
