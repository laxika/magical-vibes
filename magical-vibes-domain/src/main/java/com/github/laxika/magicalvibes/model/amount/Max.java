package com.github.laxika.magicalvibes.model.amount;

import java.util.List;

/**
 * The maximum of the component amounts (the "floor" sibling of {@link Min}). Each component
 * is evaluated independently and the largest value wins, so "gain X life, where X is the
 * number of cards in your hand minus 4" (never negative) =
 * {@code Max(Fixed(0), Sum(CardsInHand(CONTROLLER), Fixed(-4)))}. Evaluates to 0 when empty.
 */
public record Max(List<DynamicAmount> amounts) implements DynamicAmount {

    public Max(DynamicAmount... amounts) {
        this(List.of(amounts));
    }
}
