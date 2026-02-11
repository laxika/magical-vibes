package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record HandDrawnMessage(MessageType type, List<CardView> hand, int mulliganCount) {

    public HandDrawnMessage(List<CardView> hand, int mulliganCount) {
        this(MessageType.HAND_DRAWN, hand, mulliganCount);
    }
}
