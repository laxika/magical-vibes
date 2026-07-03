package com.github.laxika.magicalvibes.model.interaction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Groups multi-permanent choice and multi-graveyard choice state.
 */
public class MultiSelectionState {

    // Multi-permanent choice
    private UUID multiPermanentPlayerId;
    private Set<UUID> multiPermanentValidIds;
    private int multiPermanentMaxCount;

    // Multi-graveyard choice
    private UUID multiGraveyardPlayerId;
    private Set<UUID> multiGraveyardValidCardIds;
    private int multiGraveyardMaxCount;

    public MultiSelectionState() {
    }

    // --- Multi-permanent choice ---

    public void setMultiPermanent(UUID playerId, Set<UUID> validIds, int maxCount) {
        this.multiPermanentPlayerId = playerId;
        this.multiPermanentValidIds = validIds;
        this.multiPermanentMaxCount = maxCount;
    }

    public void clearMultiPermanent() {
        this.multiPermanentPlayerId = null;
        this.multiPermanentValidIds = null;
        this.multiPermanentMaxCount = 0;
    }

    public UUID multiPermanentPlayerId() {
        return multiPermanentPlayerId;
    }

    public Set<UUID> multiPermanentValidIds() {
        return multiPermanentValidIds;
    }

    public int multiPermanentMaxCount() {
        return multiPermanentMaxCount;
    }

    // --- Multi-graveyard choice ---

    public void setMultiGraveyard(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.multiGraveyardPlayerId = playerId;
        this.multiGraveyardValidCardIds = validCardIds;
        this.multiGraveyardMaxCount = maxCount;
    }

    public void clearMultiGraveyard() {
        this.multiGraveyardPlayerId = null;
        this.multiGraveyardValidCardIds = null;
        this.multiGraveyardMaxCount = 0;
    }

    public UUID multiGraveyardPlayerId() {
        return multiGraveyardPlayerId;
    }

    public Set<UUID> multiGraveyardValidCardIds() {
        return multiGraveyardValidCardIds;
    }

    public int multiGraveyardMaxCount() {
        return multiGraveyardMaxCount;
    }

    public MultiSelectionState deepCopy() {
        MultiSelectionState copy = new MultiSelectionState();
        copy.multiPermanentPlayerId = this.multiPermanentPlayerId;
        copy.multiPermanentValidIds = this.multiPermanentValidIds != null ? new HashSet<>(this.multiPermanentValidIds) : null;
        copy.multiPermanentMaxCount = this.multiPermanentMaxCount;
        copy.multiGraveyardPlayerId = this.multiGraveyardPlayerId;
        copy.multiGraveyardValidCardIds = this.multiGraveyardValidCardIds != null ? new HashSet<>(this.multiGraveyardValidCardIds) : null;
        copy.multiGraveyardMaxCount = this.multiGraveyardMaxCount;
        return copy;
    }
}
