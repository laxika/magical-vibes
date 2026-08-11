package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

public record RevealPermanentMessage(MessageType type, CardView card, String playerName) {

    public RevealPermanentMessage(CardView card, String playerName) {
        this(MessageType.REVEAL_PERMANENT, card, playerName);
    }
}
