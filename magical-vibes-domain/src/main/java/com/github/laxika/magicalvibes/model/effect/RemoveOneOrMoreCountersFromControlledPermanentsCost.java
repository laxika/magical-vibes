package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Cost for removing a player-chosen positive number of counters from matching permanents the player controls. */
public record RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType counterType,
                                                                  PermanentPredicate permanentPredicate)
        implements CostEffect {
}
