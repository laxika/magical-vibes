package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "You may look at the top card of your library any time."
 * While a permanent with this effect is on the battlefield, only the controller
 * can see the top card of their library (not revealed to opponents).
 * <p>
 * This is distinct from {@link PlayWithTopCardRevealedEffect}, which reveals the
 * top card to all players.
 */
public record LookAtTopCardOfOwnLibraryEffect() implements CardEffect {
}
