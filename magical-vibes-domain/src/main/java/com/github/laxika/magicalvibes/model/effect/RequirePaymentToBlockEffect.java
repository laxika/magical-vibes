package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Board-wide static block tax: creatures can't block unless their controller pays
 * {@code amountPerBlocker} generic mana for each of those creatures. Charged once per
 * creature declared as a blocker, no matter how many attackers it blocks.
 *
 * <p>{@code activeCondition} (nullable, {@code null} = always active) gates the tax on a
 * state of the source permanent, re-checked at declare-blockers time — Archangel of Tithes
 * taxes only "as long as this creature is attacking".
 *
 * <p>Block-side counterpart of {@link RequirePaymentToAttackEffect}; contrast the
 * blocker-scoped {@link BlockCostEffect} family (Hipparion), which is read off the blocker
 * itself and keyed to the attacker's power.
 */
public record RequirePaymentToBlockEffect(int amountPerBlocker,
                                          Condition activeCondition) implements CardEffect {

    public RequirePaymentToBlockEffect(int amountPerBlocker) {
        this(amountPerBlocker, null);
    }
}
