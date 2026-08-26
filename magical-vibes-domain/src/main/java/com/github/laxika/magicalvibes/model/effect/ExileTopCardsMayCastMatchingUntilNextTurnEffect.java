package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles cards from the top of the controller's library and grants permission to cast the cards
 * matching {@code filter} until the end of the controller's next turn.
 */
public record ExileTopCardsMayCastMatchingUntilNextTurnEffect(DynamicAmount count,
                                                               CardPredicate filter)
        implements CardEffect {

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(int count, CardPredicate filter) {
        this(new Fixed(count), filter);
    }
}
