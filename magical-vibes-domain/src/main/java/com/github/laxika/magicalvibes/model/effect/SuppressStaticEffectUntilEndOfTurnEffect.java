package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Suppresses one printed static effect on the source permanent until end of turn.
 *
 * @param effectType the concrete static effect type to suppress
 */
public record SuppressStaticEffectUntilEndOfTurnEffect(
        Class<? extends CardEffect> effectType
) implements CardEffect {

    public SuppressStaticEffectUntilEndOfTurnEffect {
        Objects.requireNonNull(effectType, "effectType");
    }
}
