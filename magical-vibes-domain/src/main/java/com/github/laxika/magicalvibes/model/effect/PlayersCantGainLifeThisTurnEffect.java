package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, no player can gain life. Sets the
 * {@code GameData.playersCantGainLifeThisTurn} flag (cleared at turn cleanup). Unlike the static
 * {@link PlayersCantGainLifeEffect} (which relies on a permanent staying on the battlefield), this
 * is used by spells such as Skullcrack.
 */
public record PlayersCantGainLifeThisTurnEffect() implements CardEffect {
}
