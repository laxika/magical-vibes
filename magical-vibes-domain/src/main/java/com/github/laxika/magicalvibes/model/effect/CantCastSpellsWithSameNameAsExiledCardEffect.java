package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can cast spells with the same name as the card exiled
 * by the source permanent (tracked via exileReturnOnPermanentLeave).
 * Used by Exclusion Ritual.
 */
public record CantCastSpellsWithSameNameAsExiledCardEffect() implements CardEffect {
}
