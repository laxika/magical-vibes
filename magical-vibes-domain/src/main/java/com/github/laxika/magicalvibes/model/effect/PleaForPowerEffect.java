package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player votes for time or knowledge. A time majority grants the controller an extra turn;
 * otherwise, including a tie, the controller draws three cards.
 */
public record PleaForPowerEffect() implements CardEffect {
}
