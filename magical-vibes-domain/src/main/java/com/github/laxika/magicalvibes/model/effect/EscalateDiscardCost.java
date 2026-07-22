package com.github.laxika.magicalvibes.model.effect;

/**
 * Escalate additional cast cost: "Escalate—Discard a card" (CR 702.124). For each mode chosen
 * beyond the first, the caster must discard one card. Satisfiable with zero discards (casting
 * with a single mode); concrete cast validation requires {@code modesChosen - 1} discard picks.
 */
public record EscalateDiscardCost() implements CostEffect {
}
