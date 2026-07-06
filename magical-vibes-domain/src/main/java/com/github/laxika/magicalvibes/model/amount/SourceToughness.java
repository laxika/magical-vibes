package com.github.laxika.magicalvibes.model.amount;

/**
 * The source permanent's effective toughness at evaluation time, never negative.
 * Evaluation uses the live source permanent when it is still on the battlefield,
 * else its last-known snapshot (CR 608.2h last-known information). Evaluates to 0
 * when no source is known at all.
 */
public record SourceToughness() implements DynamicAmount {
}
