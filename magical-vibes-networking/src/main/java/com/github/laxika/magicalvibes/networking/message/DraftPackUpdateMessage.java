package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record DraftPackUpdateMessage(
        MessageType type,
        List<CardView> pack,
        int packNumber,
        int pickNumber,
        List<CardView> pool
) {
    public DraftPackUpdateMessage(List<CardView> pack, int packNumber, int pickNumber, List<CardView> pool) {
        this(MessageType.DRAFT_PACK_UPDATE, pack, packNumber, pickNumber, pool);
    }
}
