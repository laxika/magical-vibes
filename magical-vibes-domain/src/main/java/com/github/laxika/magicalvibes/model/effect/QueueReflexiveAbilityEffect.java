package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Puts {@code effect} onto the stack as a reflexive triggered ability.
 *
 * <p>This is used as a resolution continuation after an asynchronous action such as surveil has
 * completed. The wrapped effect is not targeted until its own triggered ability resolves.</p>
 *
 * @param effect the effect of the reflexive triggered ability
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget) implements CardEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }
}
