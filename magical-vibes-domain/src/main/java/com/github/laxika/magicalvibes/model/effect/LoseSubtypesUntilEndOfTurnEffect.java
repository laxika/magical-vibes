package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * One-shot self effect: the source permanent loses the given subtypes until end of turn
 * (e.g. Haunted Plate Mail "…that's no longer an Equipment"). Writes into
 * {@code Permanent.transientRemovedSubtypes}, which every subtype query and the layered pass
 * honour; cleared by {@code resetModifiers()}.
 */
public record LoseSubtypesUntilEndOfTurnEffect(Set<CardSubtype> subtypes) implements CardEffect {

    public LoseSubtypesUntilEndOfTurnEffect {
        subtypes = Set.copyOf(subtypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
