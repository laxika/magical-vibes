package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.function.UnaryOperator;

/**
 * Board-wide static block tax: creatures can't block unless their controller pays
 * {@code amountPerBlocker} generic mana for each of those creatures. Charged once per
 * creature declared as a blocker, no matter how many attackers it blocks.
 *
 * <p>{@code activeCondition} (nullable, {@code null} = always active) gates the tax on a
 * state of the source permanent, re-checked at declare-blockers time — Archangel of Tithes
 * taxes only "as long as this creature is attacking".
 *
 * <p>A resolution floats it with {@link GrantStaticEffectToPlayerEffect} on its controller: "this turn,
 * creatures can't block unless their controller pays {X} for each blocking creature they control" (War
 * Cadence). A floated copy is as board-wide as a printed one, whichever player it floats on; its amount is
 * fixed as the float begins (CR 608.2h) and it carries no {@code activeCondition}.
 *
 * <p>Block-side counterpart of {@link RequirePaymentToAttackEffect}; contrast the
 * blocker-scoped {@link BlockCostEffect} family (Hipparion), which is read off the blocker
 * itself and keyed to the attacker's power.
 */
public record RequirePaymentToBlockEffect(DynamicAmount amountPerBlocker,
                                          Condition activeCondition) implements AmountsFixedWhenFloatedEffect {

    public RequirePaymentToBlockEffect(DynamicAmount amountPerBlocker) {
        this(amountPerBlocker, null);
    }

    public RequirePaymentToBlockEffect(int amountPerBlocker, Condition activeCondition) {
        this(new Fixed(amountPerBlocker), activeCondition);
    }

    public RequirePaymentToBlockEffect(int amountPerBlocker) {
        this(new Fixed(amountPerBlocker), null);
    }

    @Override
    public RequirePaymentToBlockEffect withAmountsFixed(UnaryOperator<DynamicAmount> fix) {
        return new RequirePaymentToBlockEffect(fix.apply(amountPerBlocker), activeCondition);
    }
}
