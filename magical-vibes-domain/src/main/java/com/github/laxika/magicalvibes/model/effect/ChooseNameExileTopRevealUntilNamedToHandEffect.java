package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a card name. Exile the top N cards of your library, then reveal cards from the top of
 * your library until you reveal a card with the chosen name. Put that card into your hand and
 * exile all other cards revealed this way."
 *
 * <p>Used by Demonic Consultation ({@code N = 6}) and Spoils of the Vault ({@code N = 0}, losing
 * 1 life per card exiled). Name is chosen on resolution; if the named card is never revealed (e.g.
 * it was among the initial exile), the entire remaining library is exiled.
 *
 * @param topExileCount     number of cards exiled from the top before the reveal-until dig begins
 * @param lifeLossPerExiled life lost per card exiled by this effect
 */
public record ChooseNameExileTopRevealUntilNamedToHandEffect(int topExileCount, int lifeLossPerExiled)
        implements CardEffect {

    public ChooseNameExileTopRevealUntilNamedToHandEffect(int topExileCount) {
        this(topExileCount, 0);
    }
}
