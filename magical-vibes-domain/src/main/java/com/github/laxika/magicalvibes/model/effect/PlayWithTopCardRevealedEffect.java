package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "Play with the top card of your library revealed."
 * While a permanent with this effect is on the battlefield, the controller's
 * library top card is continuously visible to all players.
 * <p>
 * This is distinct from {@link RevealTopCardOfLibraryEffect}, which is a one-shot
 * activated ability effect (e.g. Aven Windreader).
 */
public record PlayWithTopCardRevealedEffect() implements CardEffect {
}
