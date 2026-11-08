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
 *
 * <p>{@code copyOtherExiledCard} is Spellweaver Helix's "you may copy the other": it copies every card exiled with the
 * source but the first one with the same name as the stack entry's triggering spell, read as the ability resolves (the
 * live spell, else as it last existed on the stack), and nothing when no exiled card has that name.
 */
public record CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost,
                                                     boolean copyOtherExiledCard,
                                                     UUID boundImprintedCardId)
        implements ImprintedCardXCostEffect {

    public CopyImprintedCardAndMayCastCopyEffect() {
        this(true, false, null);
    }

    public CopyImprintedCardAndMayCastCopyEffect(boolean requiresImprintedXCost) {
        this(requiresImprintedXCost, false, null);
    }

    /** "You may copy the other" exiled card than the one with the triggering spell's name (Spellweaver Helix). */
    public static CopyImprintedCardAndMayCastCopyEffect otherExiledCard() {
        return new CopyImprintedCardAndMayCastCopyEffect(false, true, null);
    }

    public CopyImprintedCardAndMayCastCopyEffect boundTo(UUID imprintedCardId) {
        return new CopyImprintedCardAndMayCastCopyEffect(requiresImprintedXCost, copyOtherExiledCard, imprintedCardId);
    }

    @Override
    public boolean requiresImprintedXCost() {
        return requiresImprintedXCost;
    }
}
