package com.github.laxika.magicalvibes.model.amount;

/**
 * The source permanent's effective power at evaluation time, never negative (matches
 * the engine's power-based-damage clamp). Evaluation uses the live source permanent
 * when it is still on the battlefield, else its last-known snapshot (CR 608.2h
 * last-known information, e.g. the source was sacrificed or died in response).
 * Evaluates to 0 when no source is known at all.
 */
public record SourcePower() implements DynamicAmount {
}
