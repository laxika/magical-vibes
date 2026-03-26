package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record SaveDeckResponse(MessageType type, DeckInfo deck) {

    public static SaveDeckResponse success(DeckInfo deck) {
        return new SaveDeckResponse(MessageType.SAVE_DECK_RESPONSE, deck);
    }
}
