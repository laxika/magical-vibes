package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to the controller of the permanent that caused the trigger.
 * The target player UUID is pre-set on the stack entry's targetPermanentId at trigger-collection time.
 */
public record DealDamageToTriggeringPermanentControllerEffect(int damage) implements CardEffect {
}
