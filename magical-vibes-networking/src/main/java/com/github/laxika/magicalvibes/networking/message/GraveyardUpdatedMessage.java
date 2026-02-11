package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record GraveyardUpdatedMessage(MessageType type, List<List<CardView>> graveyards) {

    public GraveyardUpdatedMessage(List<List<CardView>> graveyards) {
        this(MessageType.GRAVEYARD_UPDATED, graveyards);
    }
}
