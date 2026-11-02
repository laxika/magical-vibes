package com.github.laxika.magicalvibes.model.effect;

/**
 * Whenever a permanent deals damage to this effect's controller, return that permanent to its owner's hand.
 * Used with {@code Triggers.permanentDealsDamageToYou()}.
 */
public record ReturnDamageSourcePermanentToHandEffect() implements CardEffect {
}
