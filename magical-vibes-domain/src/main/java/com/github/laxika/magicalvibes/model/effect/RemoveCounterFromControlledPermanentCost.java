package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Objects;

/**
 * Cost effect that removes a single allowed counter from any permanent the payer controls
 * ("unless you remove a counter from a permanent you control" — Chisei, Heart of Oceans).
 *
 * <p>Unlike {@link RemoveCounterFromControlledCreatureCost} this is not restricted to creatures
 * and can be restricted to one or more counter types. With the no-argument constructor, any
 * permanent carrying at least one counter is a legal choice. When the chosen permanent carries
 * several allowed kinds of counters, the first kind present is removed (same convention as
 * {@link MoveCounterFromTargetCreatureToTargetCreatureEffect}'s "a counter" mode).
 *
 * <p>Used both as the payable side of a {@link ForcedCostOrElseEffect} and as an activated-ability
 * cost. When used for an activated ability, the controller chooses one of their counter-bearing
 * permanents through the standard permanent-choice cost flow. {@code permanentFilter}, when
 * present, further restricts which permanents may pay the cost.
 */
public record RemoveCounterFromControlledPermanentCost(List<CounterType> allowedCounterTypes,
                                                        PermanentPredicate permanentFilter) implements CostEffect {

    public RemoveCounterFromControlledPermanentCost() {
        this(List.of(CounterType.ANY), null);
    }

    public RemoveCounterFromControlledPermanentCost(List<CounterType> allowedCounterTypes) {
        this(allowedCounterTypes, null);
    }

    public RemoveCounterFromControlledPermanentCost(CounterType... allowedCounterTypes) {
        this(List.of(allowedCounterTypes), null);
    }

    public RemoveCounterFromControlledPermanentCost(CounterType counterType, PermanentPredicate permanentFilter) {
        this(List.of(counterType), permanentFilter);
    }

    public RemoveCounterFromControlledPermanentCost {
        Objects.requireNonNull(allowedCounterTypes, "allowedCounterTypes");
        allowedCounterTypes = List.copyOf(allowedCounterTypes);
    }

    public boolean allows(CounterType counterType) {
        return allowedCounterTypes.contains(CounterType.ANY) || allowedCounterTypes.contains(counterType);
    }
}
