package com.github.laxika.magicalvibes.model.effect;

/**
 * "Create the given token, then put this creature's counters on that token" (Ambitious Augmenter's Fractal). As it
 * resolves, the token enters first, then it gets the counters of every type the stack entry's triggering permanent
 * had as it last existed on the battlefield (CR 608.2h). Write "if it had one or more counters on it" as the
 * trigger's filter ({@code Triggers.dies(THIS).withFilter(new PermanentHasCountersPredicate(CounterType.ANY))}).
 *
 * @param tokenTemplate the token to create before placing the dying creature's counters
 */
public record CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
