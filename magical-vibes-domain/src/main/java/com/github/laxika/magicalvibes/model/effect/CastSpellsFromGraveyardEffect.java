package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Static effect that lets the controller cast matching spells from their graveyard for their
 * normal mana costs.
 */
public record CastSpellsFromGraveyardEffect(CardPredicate filter, List<CastingCost> additionalCosts,
                                            CounterType enterWithCounter, int enterWithCounterCount)
        implements CastSpellsFromGraveyardPermission {

    public CastSpellsFromGraveyardEffect {
        additionalCosts = additionalCosts == null ? List.of() : List.copyOf(additionalCosts);
        if (enterWithCounterCount < 0) {
            throw new IllegalArgumentException("Counter count cannot be negative");
        }
        if (enterWithCounter == null && enterWithCounterCount != 0) {
            throw new IllegalArgumentException("A counter type is required when counters are added");
        }
    }

    public CastSpellsFromGraveyardEffect(CardPredicate filter) {
        this(filter, List.of(), null, 0);
    }

    public CastSpellsFromGraveyardEffect(CardPredicate filter, List<? extends CastingCost> additionalCosts,
                                         CounterType enterWithCounter) {
        this(filter, List.copyOf(additionalCosts), enterWithCounter,
                enterWithCounter == null ? 0 : 1);
    }
}
