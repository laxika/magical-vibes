package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;

/**
 * A self-contained, Spring-managed "normal" (stack-resolution) effect handler.
 *
 * <p>Each migrated runtime effect lives in its own {@code @Component} implementing this interface
 * (the package is {@code normalfx}, mirroring the {@code staticfx} naming). The handler declares
 * which {@link CardEffect} type it handles via {@link #handledEffect()}.
 *
 * <p>Because it extends {@link EffectHandler}, the bean itself <em>is</em> the handler — the
 * registry stores it directly via {@code resolve(GameData, StackEntry, CardEffect)}.
 */
public interface NormalEffectHandlerBean extends EffectHandler {

    Class<? extends CardEffect> handledEffect();
}
