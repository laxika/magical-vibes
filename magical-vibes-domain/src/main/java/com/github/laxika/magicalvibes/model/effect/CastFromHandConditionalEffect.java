package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

/**
 * Conditional wrapper that resolves its inner effect only when the spell or permanent was cast from a source zone.
 * Defaults to hand for existing "if you cast it from your hand" patterns.
 *
 * @param sourceZone source zone required for the condition
 * @param wrapped the inner effect to resolve when the source-zone condition is met
 */
public record CastFromHandConditionalEffect(Zone sourceZone, CardEffect wrapped) implements ConditionalEffect {

    public CastFromHandConditionalEffect(CardEffect wrapped) {
        this(Zone.HAND, wrapped);
    }

    @Override
    public String conditionName() {
        return "cast from " + sourceZone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "spell or permanent was not cast from " + sourceZone.name().toLowerCase();
    }
}
