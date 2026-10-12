package com.github.laxika.magicalvibes.model.amount;

/**
 * An amount that is determined only once per resolution of the spell or ability that uses it
 * (CR 608.2h): the first time it is evaluated against a stack entry the result is remembered, and
 * every later evaluation of an equal {@code DeterminedOnce} on that same entry reuses it. Used when
 * several instructions share one "where X is ..." definition and an earlier instruction changes the
 * game state the definition reads, e.g. "Target player loses X life and you gain X life, where X is
 * the greatest power among creatures you control". Without a stack entry the inner amount is
 * evaluated normally.
 */
public record DeterminedOnce(DynamicAmount amount) implements DynamicAmount {
}
