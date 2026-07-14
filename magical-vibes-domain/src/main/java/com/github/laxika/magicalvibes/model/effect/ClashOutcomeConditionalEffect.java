package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for a clash-trigger effect whose wrapped effect is applied only on one clash
 * outcome (CR 701.29c "If you won, …" and its mirror). Lets {@code TriggerCollectionService} resolve
 * the won/lost clause the moment a clash ends without branching on the two concrete wrapper types,
 * reading pure facts (the wrapped effect and the outcome it fires on).
 *
 * <p>Descriptive only: both facts are drawn from the record's existing components. Targeting still
 * delegates to the wrapped effect on the concrete records themselves.
 */
public interface ClashOutcomeConditionalEffect extends CardEffect {

    /** The effect applied only when the clash outcome matches {@link #appliesOnWin()}. */
    CardEffect wrapped();

    /** {@code true} if {@link #wrapped()} applies on a won clash, {@code false} if on a lost/tied clash. */
    boolean appliesOnWin();
}
