package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that gives permanents in {@code scope} the name and creature type chosen on the
 * source permanent. The chosen creature type replaces the permanent's other creature types.
 */
public record SetChosenNameAndCreatureTypeEffect(GrantScope scope) implements CardEffect {
}
