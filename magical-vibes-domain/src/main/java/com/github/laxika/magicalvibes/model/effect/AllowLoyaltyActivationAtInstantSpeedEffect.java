package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker allowing loyalty abilities to be activated on any player's turn any time the
 * activating player could cast an instant. On a planeswalker it applies to that planeswalker; on
 * an emblem it applies to planeswalkers controlled by the emblem's controller.
 */
public record AllowLoyaltyActivationAtInstantSpeedEffect() implements CardEffect {
}
