package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.Objects;

/**
 * A static ability (CR 113.3d): a statement that is simply true, and creates continuous effects
 * while its object is in one of the zones the ability functions from.
 *
 * @param effects the continuous effects it creates, at least one
 * @param zones   the zones it functions from
 */
public record StaticAbility(List<CardEffect> effects, AbilityZones zones) implements Ability {

    public StaticAbility {
        effects = List.copyOf(Objects.requireNonNull(effects, "effects"));
        Objects.requireNonNull(zones, "zones");
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A static ability needs at least one effect");
        }
    }

    /** A static ability of a permanent, which functions on the battlefield (CR 113.6). */
    public StaticAbility(List<CardEffect> effects) {
        this(effects, AbilityZones.BATTLEFIELD);
    }
}
