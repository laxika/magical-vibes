package com.github.laxika.magicalvibes.model.effect;

/** Static restriction preventing a matching face-down permanent from being turned face up. */
public record CantBeTurnedFaceUpEffect(GrantScope scope) implements CardEffect {
}
