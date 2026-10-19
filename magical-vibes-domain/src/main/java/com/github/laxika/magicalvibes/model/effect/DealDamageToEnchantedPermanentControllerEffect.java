package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals non-targeted damage to the current controller of the permanent attached to the source Aura.
 *
 * @param amount                   the damage dealt
 * @param enchantedPermanentIsSource when true, the enchanted permanent (last known information if it has
 *                                 died) is the damage source ("it deals 1 damage to its controller") instead
 *                                 of the Aura
 */
public record DealDamageToEnchantedPermanentControllerEffect(DynamicAmount amount, boolean enchantedPermanentIsSource)
        implements DamageDealingEffect {

    public DealDamageToEnchantedPermanentControllerEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public DealDamageToEnchantedPermanentControllerEffect(int damage) {
        this(new Fixed(damage));
    }

    /** Damage dealt by the enchanted permanent itself rather than by the Aura. */
    public static DealDamageToEnchantedPermanentControllerEffect dealtByEnchantedPermanent(int damage) {
        return new DealDamageToEnchantedPermanentControllerEffect(new Fixed(damage), true);
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
