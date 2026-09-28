package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of distinct mana values among matching non-token cards in the scoped graveyard(s). */
public record DistinctManaValuesAmongCardsInGraveyard(
        CountScope scope, boolean nonlandOnly, CardPredicate filter) implements DynamicAmount {
    public DistinctManaValuesAmongCardsInGraveyard(CountScope scope, boolean nonlandOnly) {
        this(scope, nonlandOnly, null);
    }

    public DistinctManaValuesAmongCardsInGraveyard(CountScope scope) {
        this(scope, false, null);
    }


    public DistinctManaValuesAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER, false, null);
    }
}
