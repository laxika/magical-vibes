package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: the controller's opponents and permanents they control with hexproof can be
 * targeted by the controller's spells and abilities as though they didn't have hexproof. Shroud
 * and protection are unaffected.
 */
public record IgnoreOpponentHexproofEffect() implements CardEffect {
}
