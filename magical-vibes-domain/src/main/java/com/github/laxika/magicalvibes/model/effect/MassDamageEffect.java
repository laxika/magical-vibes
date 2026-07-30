package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Damage to every creature (optionally planeswalkers and players too).
 *
 * <p>When {@code perCreatureAmount} is set, {@code amount} is evaluated once per damaged creature
 * with that creature as the amount's source permanent, so source-relative amounts describe the
 * creature being damaged — e.g. Baki's Curse ("2 damage to each creature for each Aura attached to
 * that creature"). Players are never damaged in that mode, since the amount is creature-relative.
 */
public record MassDamageEffect(
        DynamicAmount amount,
        boolean damagesPlayers,
        boolean damagesPlaneswalkers,
        PermanentPredicate filter,
        boolean perCreatureAmount
) implements BoardWipeEffect {

    /** Canonical single-amount form (the amount is evaluated once, source-relative). */
    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter) {
        this(amount, damagesPlayers, damagesPlaneswalkers, filter, false);
    }

    /** Fixed damage to all creatures only (e.g. Pyroclasm) */
    public MassDamageEffect(int damage) {
        this(new Fixed(damage), false, false, null);
    }

    /** Mass damage always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
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
