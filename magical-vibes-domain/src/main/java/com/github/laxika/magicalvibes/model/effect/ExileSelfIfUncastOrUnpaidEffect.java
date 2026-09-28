package com.github.laxika.magicalvibes.model.effect;

/** Marks a permanent's own entry replacement: exile it if it was not cast or no mana was spent. */
public record ExileSelfIfUncastOrUnpaidEffect() implements CardEffect {
}
