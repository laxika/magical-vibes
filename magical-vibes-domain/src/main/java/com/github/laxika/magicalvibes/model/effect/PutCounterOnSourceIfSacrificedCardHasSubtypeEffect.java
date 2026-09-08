package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts counters on the source permanent when the card sacrificed for the activated ability has
 * the specified subtype.
 */
public record PutCounterOnSourceIfSacrificedCardHasSubtypeEffect(CardSubtype subtype,
                                                                  CounterType counterType,
                                                                  int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
