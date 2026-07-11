package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * The number of counters of the given type on a specific linked permanent identified by
 * its ID (as opposed to {@link CountersOnSource}, which reads the source permanent itself).
 * Models token CDAs that reference another permanent, e.g. Gutter Grime's Ooze tokens whose
 * power and toughness are each equal to the number of slime counters on Gutter Grime.
 * Evaluates to 0 when the linked permanent has left the battlefield.
 */
public record CountersOnLinkedPermanent(CounterType counterType, UUID linkedPermanentId) implements DynamicAmount {
}
