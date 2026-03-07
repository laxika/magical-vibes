package com.github.laxika.magicalvibes.model.interaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CardChoiceState {

    private UUID playerId;
    private Set<Integer> validIndices;
    private UUID targetPermanentId;

    public CardChoiceState(UUID playerId, Set<Integer> validIndices, UUID targetPermanentId) {
        this.playerId = playerId;
        this.validIndices = validIndices;
        this.targetPermanentId = targetPermanentId;
    }

    public UUID playerId() {
        return playerId;
    }

    public Set<Integer> validIndices() {
        return validIndices;
    }

    public UUID targetPermanentId() {
        return targetPermanentId;
    }

    public CardChoiceState deepCopy() {
        return new CardChoiceState(
                playerId,
                validIndices != null ? new HashSet<>(validIndices) : null,
                targetPermanentId
        );
    }
}
