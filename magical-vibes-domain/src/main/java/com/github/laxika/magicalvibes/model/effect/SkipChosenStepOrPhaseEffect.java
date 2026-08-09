package com.github.laxika.magicalvibes.model.effect;

/** Records that the affected player skips every remaining instance of a chosen step or phase this turn. */
public record SkipChosenStepOrPhaseEffect(SkipStepOrPhaseKind kind) implements CardEffect {
}
