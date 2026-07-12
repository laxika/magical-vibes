package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the source permanent's controller has protection from the card name
 * chosen by the source permanent (tracked via {@code permanent.chosenName}). The player
 * can't be targeted, dealt damage, or enchanted by anything with that name.
 * Used by Runed Halo.
 */
public record PlayerHasProtectionFromChosenNameEffect() implements CardEffect {
}
