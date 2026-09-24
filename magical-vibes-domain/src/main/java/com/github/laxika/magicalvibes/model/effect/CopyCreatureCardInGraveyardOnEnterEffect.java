package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.Set;

/**
 * Replacement effect for a creature that may enter as a copy of a creature card in a graveyard.
 * The selected card's post-entry handling is controlled by
 * {@code exileCopiedGraveyardCardAfterEntry}.
 */
public record CopyCreatureCardInGraveyardOnEnterEffect(
        String nameOverride,
        Integer powerOverride,
        Integer toughnessOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        CardPredicate cardFilter,
        boolean controllerGraveyardOnly,
        boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn,
        boolean grantHaste,
        boolean exileCopiedGraveyardCardAfterEntry,
        boolean exileTwoAndAddOtherPowerCounters) implements ReplacementEffect {

    public CopyCreatureCardInGraveyardOnEnterEffect(
            String nameOverride,
            int powerOverride,
            int toughnessOverride,
            Set<CardSubtype> additionalSubtypesOverride) {
        this(nameOverride, powerOverride, toughnessOverride, additionalSubtypesOverride,
                null, false, false, false, true, false);
    }

    public CopyCreatureCardInGraveyardOnEnterEffect(
            String nameOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            Set<CardSubtype> additionalSubtypesOverride,
            boolean exileTwoAndAddOtherPowerCounters) {
        this(nameOverride, powerOverride, toughnessOverride, additionalSubtypesOverride,
                null, false, false, false, true, exileTwoAndAddOtherPowerCounters);
    }

    public CopyCreatureCardInGraveyardOnEnterEffect(
            String nameOverride,
            Integer powerOverride,
            Integer toughnessOverride,
            Set<CardSubtype> additionalSubtypesOverride,
            CardPredicate cardFilter,
            boolean controllerGraveyardOnly,
            boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn,
            boolean grantHaste,
            boolean exileCopiedGraveyardCardAfterEntry) {
        this(nameOverride, powerOverride, toughnessOverride, additionalSubtypesOverride,
                cardFilter, controllerGraveyardOnly, onlyCardsPutIntoGraveyardFromLibraryThisTurn,
                grantHaste, exileCopiedGraveyardCardAfterEntry, false);
    }

    public CopyCreatureCardInGraveyardOnEnterEffect {
        additionalSubtypesOverride = Set.copyOf(additionalSubtypesOverride);
    }
}
