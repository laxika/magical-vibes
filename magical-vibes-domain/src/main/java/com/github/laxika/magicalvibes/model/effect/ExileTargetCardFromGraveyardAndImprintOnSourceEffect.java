package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Exiles the targeted card from a graveyard and imprints it on the source permanent
 * (tracked in permanentExiledCards). The card must match the required type (if non-null).
 * Used by cards like Myr Welder.
 */
public record ExileTargetCardFromGraveyardAndImprintOnSourceEffect(CardType requiredType) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
