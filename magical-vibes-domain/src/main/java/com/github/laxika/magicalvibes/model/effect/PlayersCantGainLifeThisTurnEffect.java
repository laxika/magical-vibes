package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, no player can gain life, a floating effect on every player that
 * lasts until end of turn. Unlike the static
 * {@link PlayersCantGainLifeEffect} (which relies on a permanent staying on the battlefield), this
 * is used by spells such as Skullcrack.
 */
public record PlayersCantGainLifeThisTurnEffect() implements CardEffect {
}
