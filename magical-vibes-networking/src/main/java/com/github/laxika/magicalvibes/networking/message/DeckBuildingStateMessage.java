package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record DeckBuildingStateMessage(
        MessageType type,
        List<CardView> pool,
        long deadlineEpochMillis,
        boolean alreadySubmitted
) {
    public DeckBuildingStateMessage(List<CardView> pool, long deadlineEpochMillis) {
        this(MessageType.DECK_BUILDING_STATE, pool, deadlineEpochMillis, false);
    }

    public DeckBuildingStateMessage(List<CardView> pool, long deadlineEpochMillis, boolean alreadySubmitted) {
        this(MessageType.DECK_BUILDING_STATE, pool, deadlineEpochMillis, alreadySubmitted);
    }
}
