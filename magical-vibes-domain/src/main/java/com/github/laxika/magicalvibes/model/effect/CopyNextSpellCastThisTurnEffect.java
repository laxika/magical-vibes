package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot delayed trigger that copies the next spell its controller casts this turn.
 * Permanent spell copies become tokens, and the copy may have new targets chosen.
 */
public record CopyNextSpellCastThisTurnEffect() implements CardEffect {
}
