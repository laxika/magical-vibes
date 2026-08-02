package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Intuition: the active {@code LIBRARY_REVEAL_CHOICE} is the targeted opponent choosing which of
 * the three revealed cards goes into the controller's hand (the rest go into their graveyard,
 * then their library is shuffled).
 *
 * @param controllerId the controller of Intuition (receives the chosen card)
 */
public record PendingIntuitionRevealChoice(UUID controllerId) implements PendingInteraction {
}
