package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;

/**
 * Applies an existing animation to the permanent created by a preceding targeted graveyard
 * return when that permanent has the specified subtype. The effect is intentionally unbound:
 * its stack entry target remains the returned card's graveyard card ID until resolution.
 */
public record AnimateReturnedPermanentIfSubtypeEffect(CardSubtype subtype,
                                                       AnimatePermanentsEffect animation)
        implements CardEffect {

    public AnimateReturnedPermanentIfSubtypeEffect {
        Objects.requireNonNull(subtype);
        Objects.requireNonNull(animation);
    }
}
