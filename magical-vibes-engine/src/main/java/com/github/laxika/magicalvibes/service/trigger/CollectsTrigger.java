package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a trigger collector handler for a specific (EffectSlot, CardEffect) combination.
 * <p>
 * Handler method must have signature:
 * <pre>
 *   (TriggerMatchContext, ConcreteEffect, TriggerContext)
 * </pre>
 * where {@code ConcreteEffect} is a subtype of {@link CardEffect}.
 * <p>
 * Return {@code true} if the trigger fired, {@code false} if conditions were not met.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(CollectsTriggers.class)
public @interface CollectsTrigger {
    /**
     * The CardEffect class this handler processes.
     * Use {@code CardEffect.class} to register a default/fallback handler for a slot.
     */
    Class<? extends CardEffect> value();

    /**
     * The EffectSlot this handler is registered for.
     */
    EffectSlot slot();
}
