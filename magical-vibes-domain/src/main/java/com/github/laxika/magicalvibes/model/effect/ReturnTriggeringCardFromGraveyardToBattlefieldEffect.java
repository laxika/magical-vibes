package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, returns the triggering card (the stack entry's card) from its owner's graveyard to
 * the battlefield under its owner's control, if it is still there. Fizzles silently otherwise.
 * <p>
 * Pushed as a triggered ability by the death pipeline for delayed "return that card when it dies this
 * turn" effects (e.g. Graceful Reprieve).
 */
public record ReturnTriggeringCardFromGraveyardToBattlefieldEffect() implements CardEffect {
}
