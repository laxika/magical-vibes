package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a trigger collector handler for a {@link SpellAbilityWatch} and a {@link CardEffect} class, as
 * {@link CollectsTrigger} does for an {@code EffectSlot}. The handler signature is the same.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(CollectsSpellAbilityTriggers.class)
public @interface CollectsSpellAbilityTrigger {
    /** The CardEffect class this handler processes; {@code CardEffect.class} for the watch's default handler. */
    Class<? extends CardEffect> value();

    /** The old slot collector this handler belongs to. */
    SpellAbilityWatch watch();
}
