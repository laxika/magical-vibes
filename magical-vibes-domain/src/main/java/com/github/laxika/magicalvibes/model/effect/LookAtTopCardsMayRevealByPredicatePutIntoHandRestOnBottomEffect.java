package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(
        int count,
        CardPredicate predicate,
        boolean anyNumber
) implements CardEffect {

    public LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(int count, CardPredicate predicate) {
        this(count, predicate, false);
    }
}
