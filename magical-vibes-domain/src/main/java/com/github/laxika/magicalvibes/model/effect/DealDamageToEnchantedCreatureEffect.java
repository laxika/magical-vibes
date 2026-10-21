package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals non-targeted damage to the creature enchanted by the source Aura.
 *
 * <p>By default the damaged creature is the one the trigger recorded when it was put on the stack
 * ("that creature"). With {@code currentlyEnchanted} it is whichever creature the Aura enchants as
 * the ability resolves ("enchanted creature"), so an Aura that moved in response damages its new
 * host (Tilonalli's Crown).
 */
public record DealDamageToEnchantedCreatureEffect(DynamicAmount amount, boolean currentlyEnchanted)
        implements DamageDealingEffect {

    public DealDamageToEnchantedCreatureEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public DealDamageToEnchantedCreatureEffect(int damage) {
        this(new Fixed(damage), false);
    }

    /** "Deals {@code damage} damage to enchanted creature", read when the ability resolves. */
    public static DealDamageToEnchantedCreatureEffect toCurrentlyEnchanted(int damage) {
        return new DealDamageToEnchantedCreatureEffect(new Fixed(damage), true);
    }

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
