package com.github.laxika.magicalvibes.model.effect;

/**
 * Dash's delayed return rider. The enter-the-battlefield pipeline turns this into
 * {@link ReturnSelfToHandAtEndStepEffect} only when the permanent's dash cost was paid.
 */
public record ReturnSelfToHandIfDashCostPaidEffect() implements CardEffect {
}
