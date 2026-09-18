package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player votes for time or money. Each time vote grants the controller an extra turn, and
 * each money vote lets the controller gain control of a permanent owned by that voter.
 */
public record ExpropriateEffect() implements CardEffect {
}
