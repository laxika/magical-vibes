package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. The controller may play that card
 * without paying its mana cost.
 *
 * <p>{@code notPlayedDestination} says where a card that isn't (or can't be) played goes:
 * {@link LookDestination#EXILE} exiles it (Djinn of Wishes), {@link LookDestination#TOP_OF_LIBRARY}
 * leaves it where it is (Leaf-Crowned Elder's Kinship reveal, where the card has already been
 * revealed), and {@link LookDestination#BOTTOM_OF_LIBRARY} bottoms it (Descendants' Path).</p>
 *
 * <p>When {@code requireCreatureSharingTypeWithYourCreatures} is set, only a creature card that
 * shares a creature type with a creature the controller controls may be played; anything else goes
 * straight to {@code notPlayedDestination} without offering a choice (Descendants' Path).</p>
 */
public record RevealTopCardMayPlayFreeEffect(LookDestination notPlayedDestination,
                                             boolean requireCreatureSharingTypeWithYourCreatures)
        implements CardEffect {

    public RevealTopCardMayPlayFreeEffect(LookDestination notPlayedDestination) {
        this(notPlayedDestination, false);
    }
}
