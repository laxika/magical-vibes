package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;

/**
 * A self-contained, Spring-managed static effect handler.
 *
 * <p>Each migrated static effect lives in its own {@code @Component} implementing this interface
 * (the package is {@code staticfx} because {@code static} is a reserved word and cannot be a
 * package segment). The handler declares which {@link CardEffect} type it handles via
 * {@link #handledEffect()} and whether it is a self-only (characteristic-defining) handler via
 * {@link #selfOnly()}.
 *
 * <p>Because it extends {@link StaticEffectHandler}, the bean itself <em>is</em> the handler — the
 * registry stores it directly.
 */
public interface StaticEffectHandlerBean extends StaticEffectHandler {

    Class<? extends CardEffect> handledEffect();

    default boolean selfOnly() {
        return false;
    }
}
