package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Performs surveil and then resolves a continuation after surveil completes.
 *
 * <p>The default form models wording such as "surveil N. When you do, ..." and queues a
 * reflexive triggered ability. {@link #direct(int, CardEffect)} instead keeps the continuation
 * on the resolving entry, which is useful when it needs data from the surveil choice.</p>
 *
 * @param count                the number of cards to surveil
 * @param thenEffect           the effect to resolve after surveil
 * @param queueReflexiveAbility whether the continuation is queued as a reflexive ability
 */
public record SurveilThenEffect(int count, CardEffect thenEffect, boolean queueReflexiveAbility)
        implements CardEffect {

    public SurveilThenEffect(int count, CardEffect thenEffect) {
        this(count, thenEffect, true);
    }

    /** Performs surveil and continues on the same resolving entry. */
    public static SurveilThenEffect direct(int count, CardEffect thenEffect) {
        return new SurveilThenEffect(count, thenEffect, false);
    }

    public SurveilThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
