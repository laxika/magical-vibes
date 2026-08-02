package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static, defender-side attack tax: every creature attacking this permanent's
 * controller costs {@code amountPerAttacker} generic mana more to declare.
 * The amount is evaluated from the defending player's perspective, so a
 * board-derived {@link DynamicAmount} (Sphere of Safety — enchantments you
 * control) scales with the defender's battlefield.
 */
public record RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker,
                                           boolean protectsPlaneswalkers) implements CardEffect {

    public RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker) {
        this(amountPerAttacker, true);
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }

    public RequirePaymentToAttackEffect(int amountPerAttacker, boolean protectsPlaneswalkers) {
        this(new Fixed(amountPerAttacker), protectsPlaneswalkers);
    }

    public static RequirePaymentToAttackEffect playerOnly(int amountPerAttacker) {
        return new RequirePaymentToAttackEffect(amountPerAttacker, false);
    }
}
