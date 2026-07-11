package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record MassDamageEffect(
        DynamicAmount amount,
        boolean damagesPlayers,
        boolean damagesPlaneswalkers,
        PermanentPredicate filter
) implements CardEffect {

    /** Fixed damage to all creatures only (e.g. Pyroclasm) */
    public MassDamageEffect(int damage) {
        this(new Fixed(damage), false, false, null);
    }

    /** Fixed damage to all creatures + players */
    public MassDamageEffect(int damage, boolean damagesPlayers) {
        this(new Fixed(damage), damagesPlayers, false, null);
    }

    /** Dynamic damage to all creatures + players (e.g. Ashling the Pilgrim's EventValue blast) */
    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers) {
        this(amount, damagesPlayers, false, null);
    }

    /** Backward-compatible int/X constructor (no planeswalker damage) */
    public MassDamageEffect(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter) {
        this(usesXValue ? new XValue() : new Fixed(damage), damagesPlayers, false, filter);
    }

    /** Backward-compatible int/X constructor with planeswalker damage */
    public MassDamageEffect(int damage, boolean usesXValue, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter) {
        this(usesXValue ? new XValue() : new Fixed(damage), damagesPlayers, damagesPlaneswalkers, filter);
    }
}
