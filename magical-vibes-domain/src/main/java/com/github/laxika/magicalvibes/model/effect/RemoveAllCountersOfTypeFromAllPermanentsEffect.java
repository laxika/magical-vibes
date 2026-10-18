package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Removes all counters of {@code counterType} from every permanent on every battlefield that
 * matches {@code predicate}. Corrosion's leaves-the-battlefield cleanup ("remove all rust counters
 * from all permanents") uses the unrestricted form; Quicksilver Fountain's end-step cleanup
 * ("remove all flood counters from them", where "them" are lands) restricts it to lands.
 */
public record RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType counterType,
                                                             PermanentPredicate predicate)
        implements CardEffect {

    public RemoveAllCountersOfTypeFromAllPermanentsEffect(CounterType counterType) {
        this(counterType, new PermanentTruePredicate());
    }
}
