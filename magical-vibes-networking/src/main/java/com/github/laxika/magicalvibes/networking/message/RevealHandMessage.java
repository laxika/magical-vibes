package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record RevealHandMessage(MessageType type, List<CardView> cards, String playerName) {

    public RevealHandMessage(List<CardView> cards, String playerName) {
        this(MessageType.REVEAL_HAND, cards, playerName);
    }
}
