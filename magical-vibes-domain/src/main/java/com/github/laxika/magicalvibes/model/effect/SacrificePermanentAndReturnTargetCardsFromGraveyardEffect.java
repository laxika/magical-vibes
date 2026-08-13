package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sacrifice one matching permanent, then return the chosen graveyard cards if the sacrifice
 * happened. The graveyard cards are selected as spell targets before resolution and are carried by
 * {@code StackEntry.targetCardIds}; cards that are no longer in the graveyard are ignored when at
 * least one target remains legal.
 */
public record SacrificePermanentAndReturnTargetCardsFromGraveyardEffect(
        PermanentPredicate sacrificeFilter,
        CardPredicate returnFilter,
        int targetCount,
        boolean enterTapped,
        String permanentDescription
) implements CardEffect {

    public SacrificePermanentAndReturnTargetCardsFromGraveyardEffect {
        if (targetCount <= 0) {
            throw new IllegalArgumentException("targetCount must be positive");
        }
    }
}
