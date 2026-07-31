package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of times the source permanent regenerated this turn (CR 701.15). Reads the stack
 * entry's source permanent (or its last-known snapshot when the source already left the
 * battlefield). Used by Spiny Starfish's "for each time it regenerated this turn".
 */
public record TimesSourceRegeneratedThisTurn() implements DynamicAmount {
}
