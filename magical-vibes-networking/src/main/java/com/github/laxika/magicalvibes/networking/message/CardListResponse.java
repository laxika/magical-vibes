package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record CardListResponse(MessageType type, String setCode, List<BrowseCardInfo> cards) {

    public CardListResponse(String setCode, List<BrowseCardInfo> cards) {
        this(MessageType.CARD_LIST_RESPONSE, setCode, cards);
    }
}
