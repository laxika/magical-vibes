package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, creates a floating effect allowing the controller's spells and abilities to
 * target the controller's opponents and their creatures with hexproof as though they didn't have
 * hexproof until end of turn. Shroud and protection are unaffected.
 */
public record IgnoreOpponentHexproofUntilEndOfTurnEffect() implements CardEffect {
}
