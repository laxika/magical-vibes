package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies the source permanent's imprinted (exiled) card and offers the controller a may-choice to
 * cast that copy without paying its mana cost. The imprinted card itself stays exiled, so the
 * ability can be used again; a copy that is declined, or that has no legal targets, ceases to exist
 * (CR 707.10a). Used by Elite Arcanist.
 */
public record CopyImprintedCardAndMayCastCopyEffect() implements ImprintedCardXCostEffect {
}
