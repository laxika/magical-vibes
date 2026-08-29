package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Capability for a static effect that grants creature subtypes to cards in its controller's
 * graveyard. Query code reads this fact instead of branching on a concrete effect type.
 */
public interface GraveyardSubtypeGrantingEffect extends CardEffect {

    /** Returns the subtype granted to {@code card} by {@code source}. */
    CardSubtype grantedGraveyardSubtypeFor(Permanent source, Card card);

    /** Whether this effect grants a subtype to the supplied graveyard card. */
    default boolean appliesTo(Card card) {
        return true;
    }
}
