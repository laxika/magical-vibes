package com.github.laxika.magicalvibes.service.battlefield.entertrigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Handles one kind of enter-the-battlefield trigger effect. Registered handlers replace the
 * long {@code instanceof} chains that used to live in the enter-trigger scan methods: the
 * {@link EnterTriggerHandlerRegistry} routes each effect to the handler whose {@link #handledType()}
 * it matches, falling back to a plain "put it on the stack" default when none match.
 */
public interface EnterTriggerHandler {

    /**
     * The effect type (concrete record or marker interface) this handler is responsible for.
     * Matching is by {@link Class#isInstance}, so interface markers such as
     * {@code EnterCreatureConditionalEffect} are supported.
     */
    Class<? extends CardEffect> handledType();

    void handle(EnterTriggerContext context, CardEffect effect);
}
