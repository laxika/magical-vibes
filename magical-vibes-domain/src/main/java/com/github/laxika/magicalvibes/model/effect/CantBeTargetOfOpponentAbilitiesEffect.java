package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents opponents' activated and triggered abilities from targeting this permanent.
 * Opponents' spells can still target it (unlike hexproof which blocks both).
 * <p>
 * Used by: Shanna, Sisay's Legacy
 */
public record CantBeTargetOfOpponentAbilitiesEffect() implements CardEffect {
}
