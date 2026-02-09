package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record PlayableCardsMessage(MessageType type, List<Integer> playableCardIndices) {
    public PlayableCardsMessage(List<Integer> playableCardIndices) {
        this(MessageType.PLAYABLE_CARDS_UPDATED, playableCardIndices);
    }
}
