package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static ability that lets a creature band with any number of other creatures matching a quality.
 *
 * <p>The quality is stored as a name because Master of the Hunt's token specifically says
 * "bands with other creatures named Wolves of the Hunt." Combat legality and combat-damage
 * assignment consume this fact directly; it is not ordinary {@code BANDING}. A predicate quality
 * is used for abilities such as Adventurers' Guildhouse's "bands with other legendary creatures."
 * </p>
 */
public record BandsWithOtherEffect(String creatureName, PermanentPredicate creatureFilter) implements CardEffect {

    public BandsWithOtherEffect(String creatureName) {
        this(creatureName, new PermanentNamedPredicate(creatureName));
    }

    public BandsWithOtherEffect(PermanentPredicate creatureFilter) {
        this(null, creatureFilter);
    }
}
