package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of distinct card types among non-token cards in the graveyard(s) in scope
 * (Emrakul, the Promised End — "{1} less for each card type among cards in your graveyard").
 * A single multi-type card (e.g. artifact creature) contributes each of its types.
 */
public record CardTypesAmongCardsInGraveyard(CountScope scope) implements DynamicAmount {

    public CardTypesAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER);
    }
}
