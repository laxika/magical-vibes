package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents a fixed amount of the next damage that would be dealt by the ability's source permanent this turn.
 */
public record PreventNextDamageBySelfEffect(int amount) implements CardEffect {
    public PreventNextDamageBySelfEffect() {
        this(1);
    }
}
