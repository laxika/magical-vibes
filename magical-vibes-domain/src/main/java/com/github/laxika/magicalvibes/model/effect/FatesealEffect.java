package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller looks at the top {@code count} cards of an opponent's library and may put any
 * number of them on the bottom of that library in any order, with the rest remaining on top in any
 * order (CR 701.29a).
 *
 * @param count    how many cards
 * @param opponent whose library, or {@code null} for the opponent the controller chooses as it resolves (the one
 *                 opponent, unasked)
 */
public record FatesealEffect(DynamicAmount count, PlayerSubject opponent) implements CardEffect {

    public FatesealEffect(DynamicAmount count) {
        this(count, null);
    }

    public FatesealEffect(int count) {
        this(new Fixed(count));
    }
}
