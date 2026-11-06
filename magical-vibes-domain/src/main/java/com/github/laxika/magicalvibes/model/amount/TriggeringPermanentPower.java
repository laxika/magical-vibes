package com.github.laxika.magicalvibes.model.amount;

/**
 * The power of the permanent that caused a trigger, as it last existed when the ability triggered
 * (the stack entry's {@code triggeringPermanentPowerAtTrigger}), counted as at least 0. 0 when the
 * entry recorded none. Grand Ossuary's "a number of +1/+1 counters equal to its power".
 */
public record TriggeringPermanentPower() implements DynamicAmount {
}
