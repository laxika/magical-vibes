package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Karn, Scion of Urza +1: the active {@code LIBRARY_REVEAL_CHOICE} is an opponent choosing
 * which of the two revealed cards goes to the controller's hand (the other is exiled with a
 * silver counter).
 *
 * @param controllerId the controller of Karn (receives the chosen card)
 */
public record PendingKarnScionRevealChoice(UUID controllerId) implements PendingInteraction {
}
