package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, returns the triggering card (the stack entry's card) from its owner's graveyard to
 * the battlefield (tapped if {@code enterTapped}) under its owner's control, or under the
 * triggered ability controller's control when {@code returnUnderController} is true.
 * Fizzles silently otherwise.
 * <p>
 * Pushed as a triggered ability by the death pipeline for delayed "return that card when it dies this
 * turn" effects (e.g. Graceful Reprieve untapped, Supernatural Stamina tapped).
 */
public record ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped,
                                                                   boolean returnUnderController)
        implements CardEffect {

    /** Convenience for the untapped return (Graceful Reprieve). */
    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect() {
        this(false, false);
    }

    /** Convenience for the owner's-control return with configurable tapped entry. */
    public ReturnTriggeringCardFromGraveyardToBattlefieldEffect(boolean enterTapped) {
        this(enterTapped, false);
    }
}
