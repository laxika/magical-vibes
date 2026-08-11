package com.github.laxika.magicalvibes.model.effect;

/**
 * Separates the defending player's creatures into two piles, then makes the pile chosen by that
 * player the only creatures that can block for the rest of the turn.
 */
public record SeparateCreaturesIntoPilesAndChooseBlockersEffect() implements CardEffect {
}
