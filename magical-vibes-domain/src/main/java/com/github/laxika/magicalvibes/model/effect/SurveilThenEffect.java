package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Performs surveil and, after it completes, queues a reflexive triggered ability.
 *
 * <p>This models wording such as "surveil N. When you do, ...". The continuation is inserted
 * after the surveil effect so it also works when surveil pauses for player input.</p>
 *
 * @param count      the number of cards to surveil
 * @param thenEffect the effect of the reflexive triggered ability
 */
public record SurveilThenEffect(int count, CardEffect thenEffect) implements CardEffect {

    public SurveilThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
