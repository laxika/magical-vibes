package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Marks that the active {@code LIBRARY_REVEAL_CHOICE} is the controller searching the kept pile of
 * a {@link CardPileDisposition#SEARCH_ONE_TO_HAND} separation (Phyrexian Portal). The selected card
 * goes to {@code controllerId}'s hand; every unselected card of the pile goes back into their
 * library, which is then shuffled.
 */
public record PendingPortalPileSearch(UUID controllerId) implements PendingInteraction {
}
