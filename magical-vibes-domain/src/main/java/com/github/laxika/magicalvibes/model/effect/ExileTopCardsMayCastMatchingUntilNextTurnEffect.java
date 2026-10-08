package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles cards from the top of the controller's library and grants permission to cast the cards
 * matching {@code filter} until the controller's next turn ends, or begins when requested.
 */
public record ExileTopCardsMayCastMatchingUntilNextTurnEffect(DynamicAmount count,
                                                               CardPredicate filter,
                                                               boolean expireAtTurnBeginning,
                                                               int maximumSpells)
        implements CardEffect {

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(DynamicAmount count, CardPredicate filter,
                                                         boolean expireAtTurnBeginning) {
        this(count, filter, expireAtTurnBeginning, 0);
    }

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(DynamicAmount count, CardPredicate filter) {
        this(count, filter, false);
    }

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(int count, CardPredicate filter) {
        this(new Fixed(count), filter);
    }

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(int count, CardPredicate filter,
                                                         boolean expireAtTurnBeginning) {
        this(new Fixed(count), filter, expireAtTurnBeginning, 0);
    }

    public ExileTopCardsMayCastMatchingUntilNextTurnEffect(int count, CardPredicate filter,
                                                         boolean expireAtTurnBeginning,
                                                         int maximumSpells) {
        this(new Fixed(count), filter, expireAtTurnBeginning, maximumSpells);
    }
}
