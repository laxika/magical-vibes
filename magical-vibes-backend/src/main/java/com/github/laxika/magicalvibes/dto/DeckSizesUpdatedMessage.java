package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record DeckSizesUpdatedMessage(MessageType type, List<Integer> deckSizes) {
    public DeckSizesUpdatedMessage(List<Integer> deckSizes) {
        this(MessageType.DECK_SIZES_UPDATED, deckSizes);
    }
}
