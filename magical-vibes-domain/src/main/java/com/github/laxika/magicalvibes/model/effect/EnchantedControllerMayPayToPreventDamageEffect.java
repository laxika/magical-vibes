package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Enchant-enchantment upkeep effect: the enchanted permanent's controller (the stack entry's
 * {@code targetId}, baked in by {@code StepTriggerService}) may pay any amount of mana, then this
 * source deals {@code amount} damage to that player with as much of it prevented as the mana that
 * player paid. Models Power Leak: "that player may pay any amount of mana. This Aura deals 2 damage
 * to that player. Prevent X of that damage, where X is the amount of mana that player paid."
 *
 * <p>Placed in the {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} slot. The payment
 * decision and value are made during resolution: the handler prompts the enchanted controller for X
 * (capped at {@code amount}, since prevention beyond the damage dealt is pointless), pays that much
 * generic mana from their pool, then deals {@code amount - X} damage to them through the normal
 * damage system (so other prevention/replacement effects still apply to the remainder).
 *
 * @param amount the base damage dealt before the paid-for prevention
 */
public record EnchantedControllerMayPayToPreventDamageEffect(int amount) implements DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(amount);
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
