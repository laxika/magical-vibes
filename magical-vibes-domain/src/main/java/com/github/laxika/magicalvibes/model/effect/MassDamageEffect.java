package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record MassDamageEffect(
        int damage,
        boolean usesXValue,
        boolean damagesPlayers,
        boolean damagesPlaneswalkers,
        PermanentPredicate filter
) implements CardEffect {

    /** Fixed damage to all creatures only (e.g. Pyroclasm) */
    public MassDamageEffect(int damage) {
        this(damage, false, false, false, null);
    }

    /** Fixed damage to all creatures + players */
    public MassDamageEffect(int damage, boolean damagesPlayers) {
        this(damage, false, damagesPlayers, false, null);
    }

    /** Backward-compatible 4-arg constructor (no planeswalker damage) */
    public MassDamageEffect(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter) {
        this(damage, usesXValue, damagesPlayers, false, filter);
    }
}
