package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Marks a library reveal choice where an opponent chooses one card for the controller's hand and
 * the other revealed cards go to that controller's graveyard.
 *
 * @param controllerId the player whose library was searched
 */
public record PendingOpponentChoosesCardToHandRestToGraveyard(UUID controllerId)
        implements PendingInteraction {
}
