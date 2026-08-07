package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Static, defender-side attack tax: every creature attacking this permanent's
 * controller costs {@code amountPerAttacker} generic mana more to declare.
 * The amount is evaluated from the defending player's perspective, so a
 * board-derived {@link DynamicAmount} (Sphere of Safety — enchantments you
 * control) scales with the defender's battlefield.
 *
 * <p>{@code activeCondition} (nullable, {@code null} = always active) gates the
 * whole tax on a state of the source permanent, re-checked at declare-attackers
 * time — Archangel of Tithes taxes only "as long as this creature is untapped".
 */
public record RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker,
                                           boolean protectsPlaneswalkers,
                                           Condition activeCondition) implements CardEffect {

    public RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker) {
        this(amountPerAttacker, true, null);
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker, boolean protectsPlaneswalkers) {
        this(new Fixed(amountPerAttacker), protectsPlaneswalkers, null);
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker, Condition activeCondition) {
        this(new Fixed(amountPerAttacker), true, activeCondition);
    }

    public static RequirePaymentToAttackEffect playerOnly(int amountPerAttacker) {
        return new RequirePaymentToAttackEffect(new Fixed(amountPerAttacker), false, null);
    }
}
