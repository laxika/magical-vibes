package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting-by-itself ability effect: the player carried as the stack entry's target
 * can't play lands for the rest of this turn. Cleared at end of turn.
 * Used by Moonhold (when {@code R} was spent to cast it).
 */
public record TargetPlayerCantPlayLandsThisTurnEffect() implements CardEffect {
}
