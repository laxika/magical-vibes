package com.github.laxika.magicalvibes.service.outcome;

/**
 * What {@code GameOutcomeService.resolveLoss} decided about a player who would lose the game.
 *
 * <p>Only {@link #LOSES} means the caller should go on to finish the game; the other two are
 * distinguished because callers word their game log differently (or stay silent) for each.
 */
public enum LossOutcome {

    /** No prevention or replacement applied — the player loses and the caller must finish the game. */
    LOSES,

    /**
     * A "can't lose" effect stopped the loss (Platinum Angel, Phyrexian Unlife for
     * {@link LossReason#LIFE}). Nothing was logged; the caller words its own message.
     */
    PREVENTED,

    /**
     * A {@link LossReplacer} replaced the loss with something else (Lich's Mirror's reset). The
     * replacer has already logged and mutated game state — the caller must do nothing further.
     */
    REPLACED,
}
