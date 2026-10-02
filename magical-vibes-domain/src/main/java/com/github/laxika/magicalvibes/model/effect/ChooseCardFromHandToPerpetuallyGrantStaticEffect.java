package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Optionally chooses a matching card in the controller's hand and perpetually grants it a
 * static effect. If the choice is declined or no matching card exists, the fallback resolves.
 */
public record ChooseCardFromHandToPerpetuallyGrantStaticEffect(
        CardPredicate cardFilter, CardEffect staticEffect, CardEffect fallbackEffect)
        implements CardEffect {

    public ChooseCardFromHandToPerpetuallyGrantStaticEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(staticEffect, "staticEffect");
    }
}
