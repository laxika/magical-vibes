package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents the current controller has controlled continuously since the beginning of the turn.
 * In this engine that is {@code !permanent.isSummoningSick()} — the same signal as
 * {@code CameUnderControlThisTurn} / Siren's Call's end-step exemption. Used by Norritt.
 */
public record PermanentControlledContinuouslySinceBeginningOfTurnPredicate() implements PermanentPredicate {
}
