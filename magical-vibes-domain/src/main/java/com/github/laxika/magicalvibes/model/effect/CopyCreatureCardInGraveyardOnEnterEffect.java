package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * Replacement effect for a creature that may enter as a copy of a creature card in a graveyard.
 * The selected card remains in its graveyard until the separate reflexive exile trigger resolves.
 */
public record CopyCreatureCardInGraveyardOnEnterEffect(
        String nameOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        boolean exileTwoAndAddOtherPowerCounters) implements ReplacementEffect {

    public CopyCreatureCardInGraveyardOnEnterEffect(
            String nameOverride,
            int powerOverride,
            int toughnessOverride,
            Set<CardSubtype> additionalSubtypesOverride) {
        this(nameOverride, powerOverride, toughnessOverride, additionalSubtypesOverride, false);
    }

    public CopyCreatureCardInGraveyardOnEnterEffect {
        additionalSubtypesOverride = Set.copyOf(additionalSubtypesOverride);
    }
}
