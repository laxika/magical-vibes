package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting-by-itself ability effect: the player carried as the stack entry's target
 * can't cast creature spells for the rest of this turn. Cleared at end of turn.
 * Used by Moonhold (when {@code W} was spent to cast it).
 */
public record TargetPlayerCantCastCreatureSpellsThisTurnEffect() implements CardEffect {
}
