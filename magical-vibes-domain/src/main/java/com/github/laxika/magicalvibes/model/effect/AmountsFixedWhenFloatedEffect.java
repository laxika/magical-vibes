package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.function.UnaryOperator;

/**
 * A static rule whose amounts are fixed as a resolution floats it on a player
 * ({@link GrantStaticEffectToPlayerEffect}): "this turn, creatures can't attack unless their controller pays {X}"
 * keeps the X paid for the ability, evaluated once as the effect begins (CR 608.2h). Printed on a permanent, the
 * same record keeps its amounts and they are read whenever the rule is applied.
 */
public interface AmountsFixedWhenFloatedEffect extends CardEffect {

    /** This rule with each of its amounts replaced by {@code fix}'s answer for it. */
    CardEffect withAmountsFixed(UnaryOperator<DynamicAmount> fix);
}
