package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record HandDrawnMessage(MessageType type, List<Card> hand, int mulliganCount) {

    public HandDrawnMessage(List<Card> hand, int mulliganCount) {
        this(MessageType.HAND_DRAWN, hand, mulliganCount);
    }
}
