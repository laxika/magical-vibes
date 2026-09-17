package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Tempt with Discovery's staged land searches: the controller searches first, opponents decide in
 * APNAP order, accepted opponents search, and the controller searches once for each opponent who
 * searched.
 */
public record TemptingOfferSearchLibraryForLandToBattlefieldEffect(
        List<UUID> remainingOpponentIds,
        List<UUID> acceptedOpponentIds,
        UUID abilityControllerId,
        int controllerSearchesRemaining,
        List<UUID> searchedPlayerIds
) implements CardEffect {

    public TemptingOfferSearchLibraryForLandToBattlefieldEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
        if (acceptedOpponentIds != null) {
            acceptedOpponentIds = List.copyOf(acceptedOpponentIds);
        }
        if (searchedPlayerIds != null) {
            searchedPlayerIds = List.copyOf(searchedPlayerIds);
        }
    }

    public TemptingOfferSearchLibraryForLandToBattlefieldEffect() {
        this(null, null, null, 0, null);
    }
}
