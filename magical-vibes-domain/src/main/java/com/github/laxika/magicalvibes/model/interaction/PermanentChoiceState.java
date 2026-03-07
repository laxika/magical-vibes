package com.github.laxika.magicalvibes.model.interaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PermanentChoiceState {

    private UUID playerId;
    private Set<UUID> validIds;

    public PermanentChoiceState(UUID playerId, Set<UUID> validIds) {
        this.playerId = playerId;
        this.validIds = validIds;
    }

    public UUID playerId() {
        return playerId;
    }

    public Set<UUID> validIds() {
        return validIds;
    }

    public PermanentChoiceState deepCopy() {
        return new PermanentChoiceState(
                playerId,
                validIds != null ? new HashSet<>(validIds) : null
        );
    }
}
