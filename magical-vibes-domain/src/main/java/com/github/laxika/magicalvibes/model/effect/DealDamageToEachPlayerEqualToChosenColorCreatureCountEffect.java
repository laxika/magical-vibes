package com.github.laxika.magicalvibes.model.effect;

/**
 * Prompts for a color, then deals damage to each player equal to the number of creatures of that
 * color that player controls. The amount is evaluated separately for each player.
 */
public record DealDamageToEachPlayerEqualToChosenColorCreatureCountEffect() implements CardEffect {
}
