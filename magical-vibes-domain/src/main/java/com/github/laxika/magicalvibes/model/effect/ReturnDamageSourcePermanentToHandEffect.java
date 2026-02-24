package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a permanent deals damage to this effect's controller, return that permanent to its owner's hand.
 * Used with EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.
 */
public record ReturnDamageSourcePermanentToHandEffect() implements CardEffect {
}
