package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles the sacrificed permanent's card and tracks it with the source permanent. The sacrificed permanent is the
 * stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a). When
 * {@code counterIfToken} is set and the sacrificed permanent was a token, the source permanent also gets one
 * counter of that type ("If that creature was a token, put a +1/+1 counter on this creature"), as part of the
 * same triggered ability.
 */
public record ExileSacrificedPermanentWithSourceEffect(CounterType counterIfToken) implements CardEffect {

    public ExileSacrificedPermanentWithSourceEffect() {
        this(null);
    }
}
