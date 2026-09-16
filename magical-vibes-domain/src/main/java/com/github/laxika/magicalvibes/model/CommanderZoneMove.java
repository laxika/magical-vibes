package com.github.laxika.magicalvibes.model;
import java.util.UUID;
/** A destination replacement awaiting its owner's decision; the card has not entered that destination. */
public record CommanderZoneMove(UUID ownerId, Card card, Zone destination, int libraryIndex, boolean shuffle) {
    public CommanderZoneMove shuffled() { return new CommanderZoneMove(ownerId, card, destination, libraryIndex, true); }
}
