package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Copies the source permanent's imprinted (exiled) card and offers the controller a may-choice to
 * cast that copy without paying its mana cost. The imprinted card itself stays exiled, so the
 * ability can be used again; a copy that is declined, or that has no legal targets, ceases to exist
 * (CR 707.10a). Used by Elite Arcanist and Isochron Scepter.
 *
 * <p>A non-null {@code boundImprintedCardId} pins the effect to that one exiled card, so a delayed
 * trigger registered by a spell (Surge to Victory) keeps copying the card it exiled even when the
 * same spell is cast again and imprints another card.
 */
public record CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost,
                                                     boolean copyOtherExiledCard,
                                                     UUID matchingExiledCardId,
                                                     UUID boundImprintedCardId)
        implements ImprintedCardXCostEffect {

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost,
                                                 boolean copyOtherExiledCard,
                                                 UUID matchingExiledCardId) {
        this(requiresImprintedXCost, copyOtherExiledCard, matchingExiledCardId, null);
    }

    public CopyImprintedCardAndMayCastCopyEffect() {
        this(true, false, null);
    }

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost) {
        this(requiresImprintedXCost, false, null);
    }

    public static CopyImprintedCardAndMayCastCopyEffect otherExiledCard(UUID matchingExiledCardId) {
        return new CopyImprintedCardAndMayCastCopyEffect(false, true, matchingExiledCardId);
    }

    public CopyImprintedCardAndMayCastCopyEffect boundTo(UUID imprintedCardId) {
        return new CopyImprintedCardAndMayCastCopyEffect(
                requiresImprintedXCost, copyOtherExiledCard, matchingExiledCardId, imprintedCardId);
    }

    @Override
    public boolean requiresImprintedXCost() {
        return requiresImprintedXCost;
    }
}
