package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Map;
import java.util.UUID;

/**
 * At least one of the cards is still in {@code zone}, in the stay it was in when the condition was made: "if that card
 * is in your hand", "if any of those cards remain exiled". A card that left the zone and came back is a new object
 * (CR 400.7) and doesn't count. Only {@link Zone#HAND} (the controller's hand) and {@link Zone#EXILE} (any player's
 * exile) are read.
 *
 * @param zone         the zone the cards are expected in
 * @param entryVersions each card's entry version into that zone as the condition was made
 *                      ({@code GameData.handEntryVersions} or {@code exileEntryVersions}, 0 when never recorded)
 */
public record CardsStillInZone(Zone zone, Map<UUID, Long> entryVersions) implements Condition {

    public CardsStillInZone {
        entryVersions = Map.copyOf(entryVersions);
    }

    @Override
    public String conditionName() {
        return "cards still in " + zone.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "none of the cards is still in " + zone.name().toLowerCase();
    }
}
