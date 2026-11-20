package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that sets the name of permanents matching the given subject.
 *
 * @param name  the name the affected permanent has
 * @param subject which permanents are affected
 */
public record SetNameEffect(String name, PermanentSubject subject) implements CardEffect {
}
