package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective power of the permanent the source Aura is attached to, never negative.
 * Uses the trigger-time power when the formerly enchanted permanent has left the battlefield.
 */
public record EnchantedPermanentPower() implements DynamicAmount {
}
