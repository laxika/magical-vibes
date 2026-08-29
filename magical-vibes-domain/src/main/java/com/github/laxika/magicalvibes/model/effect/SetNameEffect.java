package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that sets the name of permanents matching the given scope.
 *
 * @param name  the name the affected permanent has
 * @param scope which permanents are affected
 */
public record SetNameEffect(String name, GrantScope scope) implements CardEffect {
}
