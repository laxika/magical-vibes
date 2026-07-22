package com.github.laxika.magicalvibes.model.effect;

/**
 * Escalate additional cast cost: "Escalate {cost}" (CR 702.124). For each mode chosen beyond the
 * first, the caster must pay {@code manaCost} as part of the spell's total cost. Satisfiable with
 * a single mode (zero escalate payments); concrete casts with N modes pay the cost N-1 times.
 */
public record EscalateManaCost(String manaCost) implements CostEffect {
}
