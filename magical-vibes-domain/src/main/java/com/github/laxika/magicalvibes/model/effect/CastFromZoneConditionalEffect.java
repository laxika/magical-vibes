package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Conditional wrapper that resolves its inner effect only when the spell or permanent was cast from a source zone.
 * Pass {@link Zone#HAND} for "if you cast it from your hand" patterns, or {@link Zone#GRAVEYARD} for
 * flashback "if this spell was cast from a graveyard, do extra" patterns (e.g. Increasing Vengeance).
 *
 * @param sourceZone source zone required for the condition
 * @param wrapped the inner effect to resolve when the source-zone condition is met
 */
public record CastFromZoneConditionalEffect(Zone sourceZone, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "cast from " + sourceZone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "spell or permanent was not cast from " + sourceZone.name().toLowerCase();
    }
}
