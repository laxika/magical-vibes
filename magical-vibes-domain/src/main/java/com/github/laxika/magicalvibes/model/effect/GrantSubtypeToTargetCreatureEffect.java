package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot effect that permanently grants a subtype to the targeted creature
 * ("That creature becomes a [subtype] in addition to its other types").
 * The subtype is added to the permanent's {@code grantedSubtypes} and persists
 * across turns (not cleared by {@code resetModifiers}).
 *
 * @param subtype the subtype to grant to the target creature
 */
public record GrantSubtypeToTargetCreatureEffect(CardSubtype subtype) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
