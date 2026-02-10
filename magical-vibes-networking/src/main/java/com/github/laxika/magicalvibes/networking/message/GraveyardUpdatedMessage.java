package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record GraveyardUpdatedMessage(MessageType type, List<List<Card>> graveyards) {

    public GraveyardUpdatedMessage(List<List<Card>> graveyards) {
        this(MessageType.GRAVEYARD_UPDATED, graveyards);
    }
}
