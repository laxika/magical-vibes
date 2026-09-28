package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers an Aura card from the controller's hand and puts the chosen card onto the battlefield
 * attached to the creature that was equipped by the source Equipment when its trigger fired.
 */
public record PutAuraFromHandOntoEquippedCreatureEffect() implements CardEffect {
}
