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
        boolean perCreatureAmount,
        boolean exileInsteadOfDie,
        boolean cantRegenerate,
        boolean damagesBattles
) implements BoardWipeEffect {

    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter,
                            boolean perCreatureAmount, boolean exileInsteadOfDie,
                            boolean cantRegenerate) {
        this(amount, damagesPlayers, damagesPlaneswalkers, filter, perCreatureAmount,
                exileInsteadOfDie, cantRegenerate, false);
    }

    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter,
                            boolean perCreatureAmount) {
        this(amount, damagesPlayers, damagesPlaneswalkers, filter, perCreatureAmount, false, false);
    }

    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter,
                            boolean perCreatureAmount, boolean exileInsteadOfDie) {
        this(amount, damagesPlayers, damagesPlaneswalkers, filter, perCreatureAmount, exileInsteadOfDie, false);
    }

    /** Canonical single-amount form (the amount is evaluated once, source-relative). */
    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter) {
        this(amount, damagesPlayers, damagesPlaneswalkers, filter, false, false, false);
    }

    /**
     * "Deals N damage to each creature. If a creature dealt damage this way would die this turn,
     * exile it instead" (Yamabushi's Storm).
     */
    public static MassDamageEffect exilingDamageToEachCreature(int damage) {
        return new MassDamageEffect(new Fixed(damage), false, false, null, false, true);
    }

    /** Fixed damage to creatures, optionally players, that prevents regeneration of creatures dealt damage. */
    public static MassDamageEffect cantRegenerate(int damage, boolean damagesPlayers, PermanentPredicate filter) {
        return new MassDamageEffect(new Fixed(damage), damagesPlayers, false, filter, false, false, true);
    }

    /** Fixed damage to each creature, planeswalker, and battle. */
    public static MassDamageEffect damageToEachCreaturePlaneswalkerAndBattle(int damage) {
        return new MassDamageEffect(new Fixed(damage), false, true, null, false, false, false, true);
    }

    /** Fixed damage to all creatures only (e.g. Pyroclasm) */
    public MassDamageEffect(int damage) {
        this(new Fixed(damage), false, false, null, false, false, false);
    }

    /** Mass damage always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }

    /** Fixed damage to all creatures + players */
    public MassDamageEffect(int damage, boolean damagesPlayers) {
        this(new Fixed(damage), damagesPlayers, false, null, false, false, false);
    }

    /** Dynamic damage to all creatures + players (e.g. Ashling the Pilgrim's EventValue blast) */
    public MassDamageEffect(DynamicAmount amount, boolean damagesPlayers) {
        this(amount, damagesPlayers, false, null, false, false, false);
    }

    /** Backward-compatible int/X constructor (no planeswalker damage) */
    public MassDamageEffect(int damage, boolean usesXValue, boolean damagesPlayers, PermanentPredicate filter) {
        this(usesXValue ? new XValue() : new Fixed(damage), damagesPlayers, false, filter, false, false, false);
    }

    /** Backward-compatible int/X constructor with planeswalker damage */
    public MassDamageEffect(int damage, boolean usesXValue, boolean damagesPlayers,
                            boolean damagesPlaneswalkers, PermanentPredicate filter) {
        this(usesXValue ? new XValue() : new Fixed(damage), damagesPlayers, damagesPlaneswalkers, filter,
                false, false, false);
    }
}
