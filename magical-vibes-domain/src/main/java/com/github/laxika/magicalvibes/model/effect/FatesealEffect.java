package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller looks at the top {@code count} cards of an opponent's library and may put any
 * number of them on the bottom of that library in any order, with the rest remaining on top in any
 * order.
 */
public record FatesealEffect(DynamicAmount count) implements CardEffect {

    public FatesealEffect(int count) {
        this(new Fixed(count));
    }
}
