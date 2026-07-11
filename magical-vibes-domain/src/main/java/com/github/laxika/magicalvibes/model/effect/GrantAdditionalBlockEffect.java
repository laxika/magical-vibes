package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Can block N additional creatures". By default (null {@code controlledFilter}) the grant is
 * self-only when the source is a creature, applies to the equipped/enchanted creature when the
 * source is an Equipment/Aura, and is global when the source is any other permanent.
 *
 * <p>When {@code controlledFilter} is non-null the grant instead applies to every permanent the
 * source's controller controls that matches the predicate (e.g. Cenn's Tactician: "Each creature
 * you control with a +1/+1 counter on it can block an additional creature each combat").
 */
public record GrantAdditionalBlockEffect(int additionalBlocks, PermanentPredicate controlledFilter)
        implements CardEffect {

    public GrantAdditionalBlockEffect(int additionalBlocks) {
        this(additionalBlocks, null);
    }
}
