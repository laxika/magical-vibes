package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect for playing with one or all players' top library cards revealed.
 * While a permanent with this effect is on the battlefield, the selected library top cards
 * are continuously visible to all players.
 * <p>
 * This is distinct from {@link RevealTopCardOfLibraryEffect}, which is a one-shot
 * activated ability effect (e.g. Aven Windreader).
 */
public record PlayWithTopCardRevealedEffect(boolean allPlayers) implements CardEffect {

    public PlayWithTopCardRevealedEffect() {
        this(false);
    }

    public static PlayWithTopCardRevealedEffect forAllPlayers() {
        return new PlayWithTopCardRevealedEffect(true);
    }
}
