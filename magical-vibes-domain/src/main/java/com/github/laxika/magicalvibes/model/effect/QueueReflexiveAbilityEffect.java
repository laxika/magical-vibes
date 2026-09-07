package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Puts {@code effect} onto the stack as a reflexive triggered ability.
 *
 * <p>This is used as a resolution continuation after an asynchronous action such as surveil has
 * completed. The wrapped effect is not targeted until its own triggered ability resolves.</p>
 *
 * @param effect the effect of the reflexive triggered ability
 * @param useEventValueAsX whether the preceding event value supplies X for the reflexive ability
 */
public record QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget,
                                           boolean useEventValueAsX) implements CardEffect {

    public QueueReflexiveAbilityEffect(CardEffect effect) {
        this(effect, false, false);
    }

    public QueueReflexiveAbilityEffect(CardEffect effect, boolean optionalTarget) {
        this(effect, optionalTarget, false);
    }

    public QueueReflexiveAbilityEffect {
        Objects.requireNonNull(effect, "effect");
    }
}
