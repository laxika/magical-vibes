package com.github.laxika.magicalvibes.model.effect;

/**
 * Static hand-reveal capability. While a permanent carrying this effect is on the battlefield,
 * some hand(s) are publicly visible via {@code GameBroadcastService.getRevealedOpponentHand}.
 *
 * <ul>
 *   <li>{@code controllerOnly() == false} — every player's hand is revealed to everyone
 *       (Zur's Weirding / {@link PlayWithHandsRevealedEffect}).</li>
 *   <li>{@code controllerOnly() == true} — only the effect's controller's hand is revealed
 *       (Enduring Renewal / {@link PlayWithOwnHandRevealedEffect}).</li>
 * </ul>
 */
public interface PubliclyRevealedHandEffect extends CardEffect {

    /** {@code true} if only the controller's hand is revealed; {@code false} if all hands are. */
    boolean controllerOnly();
}
