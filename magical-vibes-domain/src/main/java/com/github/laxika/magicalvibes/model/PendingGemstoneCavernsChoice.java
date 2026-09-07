package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** State kept while Gemstone Caverns waits for its required opening-hand exile choice. */
public record PendingGemstoneCavernsChoice(Card sourceCard, UUID controllerId) {
}
