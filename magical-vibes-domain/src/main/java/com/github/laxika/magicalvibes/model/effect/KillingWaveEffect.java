package com.github.laxika.magicalvibes.model.effect;

/**
 * Killing Wave: "For each creature, its controller sacrifices it unless they pay X life."
 * X comes from the resolving stack entry's {@code xValue}. Non-targeting SPELL.
 *
 * <p>Per ruling: active player chooses for each creature they control, then each other player in
 * turn order (later players see earlier choices). Then all life payments and sacrifices happen
 * simultaneously. A player may pay for some creatures and sacrifice the rest; they can't pay more
 * life than they have. Resolved by {@code KillingWaveEffectHandler} via an APNAP multi-permanent
 * "creatures to keep (pay X life each)" choice ({@code MultiPermanentChoiceContext.KillingWaveKeep}).
 */
public record KillingWaveEffect() implements CardEffect {
}
