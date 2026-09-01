package com.github.laxika.magicalvibes.model.amount;

/** The number of distinct mana values among non-token cards in the scoped graveyard(s). */
public record DistinctManaValuesAmongCardsInGraveyard(CountScope scope) implements DynamicAmount {

    public DistinctManaValuesAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER);
    }
}
