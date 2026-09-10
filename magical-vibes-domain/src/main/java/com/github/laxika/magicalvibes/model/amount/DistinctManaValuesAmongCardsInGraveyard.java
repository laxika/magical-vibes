package com.github.laxika.magicalvibes.model.amount;

/** The number of distinct mana values among non-token cards in the scoped graveyard(s). */
public record DistinctManaValuesAmongCardsInGraveyard(CountScope scope, boolean nonlandOnly) implements DynamicAmount {
        public DistinctManaValuesAmongCardsInGraveyard(CountScope scope) {
            this(scope, false);
        }


    public DistinctManaValuesAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER, false);
    }
}
