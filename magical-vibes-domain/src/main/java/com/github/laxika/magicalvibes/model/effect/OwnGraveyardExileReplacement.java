package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability for a static replacement: if a matching card would be put into the controller's
 * graveyard from anywhere, exile it instead (CR 614.1). Read by {@code GraveyardService}.
 */
public interface OwnGraveyardExileReplacement extends CardEffect {

    /**
     * Cards that match this predicate are redirected to exile. {@code null} = every card
     * (Forbidden Crypt).
     */
    CardPredicate filter();

    /**
     * When true, the replacement does not apply to a card currently being put into the graveyard
     * as the discard cost of cycling (Abandoned Sarcophagus "and it wasn't cycled").
     */
    boolean exemptWhenCycled();

    /** When false, tokens are not redirected (Abandoned Sarcophagus ruling). Default true. */
    default boolean appliesToTokens() {
        return true;
    }
}
