package com.github.laxika.magicalvibes.model.amount;

/** The number of distinct noncreature subtypes among non-token cards in the scoped graveyards. */
public record NonCreatureSubtypesAmongCardsInGraveyard(CountScope scope) implements DynamicAmount {

    public NonCreatureSubtypesAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER);
    }
}
