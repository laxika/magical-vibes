package com.github.laxika.magicalvibes.model.interaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CardChoiceState {

    private UUID playerId;
    private Set<Integer> validIndices;
    private UUID targetId;

    public CardChoiceState(UUID playerId, Set<Integer> validIndices, UUID targetId) {
        this.playerId = playerId;
        this.validIndices = validIndices;
        this.targetId = targetId;
    }

    public UUID playerId() {
        return playerId;
    }

    public Set<Integer> validIndices() {
        return validIndices;
    }

    public UUID targetId() {
        return targetId;
    }

    public CardChoiceState deepCopy() {
        return new CardChoiceState(
                playerId,
                validIndices != null ? new HashSet<>(validIndices) : null,
                targetId
        );
    }
}
