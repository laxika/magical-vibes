package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Copies the source permanent's imprinted (exiled) card and offers the controller a may-choice to
 * cast that copy without paying its mana cost. The imprinted card itself stays exiled, so the
 * ability can be used again; a copy that is declined, or that has no legal targets, ceases to exist
 * (CR 707.10a). Used by Elite Arcanist and Isochron Scepter.
 */
public record CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost,
                                                     boolean copyOtherExiledCard,
                                                     UUID triggeringCardId)
        implements ImprintedCardXCostEffect {

    public CopyImprintedCardAndMayCastCopyEffect() {
        this(true, false, null);
    }

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost) {
        this(requiresImprintedXCost, false, null);
    }

    public static CopyImprintedCardAndMayCastCopyEffect otherExiledCard(UUID triggeringCardId) {
        return new CopyImprintedCardAndMayCastCopyEffect(false, true, triggeringCardId);
    }

    @Override
    public boolean requiresImprintedXCost() {
        return requiresImprintedXCost;
    }
}
