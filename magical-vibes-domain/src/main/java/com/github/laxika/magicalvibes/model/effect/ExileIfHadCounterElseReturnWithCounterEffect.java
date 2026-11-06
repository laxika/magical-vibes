package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "When this dies": "exile it if it had a [counter] on it. Otherwise, return it to the battlefield under your control
 * and put a [counter] on it" (Bogardan Phoenix). As it resolves, it reads whether the stack entry's triggering
 * permanent had a {@code counterType} counter as it last existed on the battlefield (CR 608.2h), and exiles the
 * triggering card from the graveyard or returns it with one {@code counterType} counter.
 *
 * @param counterType counter checked on the dying permanent and placed on the return branch
 */
public record ExileIfHadCounterElseReturnWithCounterEffect(CounterType counterType) implements CardEffect {
}
