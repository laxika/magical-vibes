package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles cards from the top of the controller's library and grants permission to play them until
 * end of turn.
 */
public record ExileTopCardsMayPlayThisTurnEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsMayPlayThisTurnEffect(int count) {
        this(new Fixed(count));
    }
}
