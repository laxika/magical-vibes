package com.github.laxika.magicalvibes.model.effect;

/**
 * Separates the active player's creatures into two piles, then makes the pile chosen by that
 * player the only creatures that can attack for the rest of the turn.
 */
public record SeparateCreaturesIntoPilesAndChooseAttackersEffect() implements CardEffect {
}
