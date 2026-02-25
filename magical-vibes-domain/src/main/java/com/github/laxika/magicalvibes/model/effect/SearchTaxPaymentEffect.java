package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the MayAbility system to identify a pending Leonin Arbiter search tax payment.
 * When the player accepts, the mana is deducted, the Arbiters are marked as paid, and the original
 * search effect is re-dispatched via a new StackEntry.
 */
public record SearchTaxPaymentEffect() implements CardEffect {
}
