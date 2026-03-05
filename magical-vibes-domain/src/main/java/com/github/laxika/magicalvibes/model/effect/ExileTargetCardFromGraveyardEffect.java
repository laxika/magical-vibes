package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Exiles the targeted card from a graveyard (any player's). The card must match the required type
 * (if non-null). Unlike {@link ExileTargetCardFromGraveyardAndImprintOnSourceEffect}, this does NOT
 * track the exiled card on the source permanent (no imprint).
 */
public record ExileTargetCardFromGraveyardEffect(CardType requiredType) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
