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
public record RequirePaymentToAttackEffect(DynamicAmount amountPerAttacker) implements CardEffect {

    public RequirePaymentToAttackEffect(int amountPerAttacker) {
        this(new Fixed(amountPerAttacker));
    }
}
