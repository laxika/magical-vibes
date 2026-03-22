package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can cast spells from graveyards.
 * Prevents flashback, graveyard cast, and any other mechanism that casts spells from graveyards.
 * Does not prevent playing lands from graveyards (lands are not spells).
 * Used by Ashes of the Abhorrent (XLN) and similar effects.
 */
public record PlayersCantCastSpellsFromGraveyardsEffect() implements CardEffect {
}
