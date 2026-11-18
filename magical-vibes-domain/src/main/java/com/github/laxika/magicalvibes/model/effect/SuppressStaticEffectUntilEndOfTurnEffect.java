package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Until end of turn, this permanent loses '[static ability]'" (Glittering Lion): the source permanent loses its
 * static abilities of {@code effectType} until end of turn, a layer-6 ability removal (CR 613.1f). As it resolves it
 * becomes a floating effect on the source with this record as its payload; a later grant of such an ability stays
 * (CR 613.7).
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
