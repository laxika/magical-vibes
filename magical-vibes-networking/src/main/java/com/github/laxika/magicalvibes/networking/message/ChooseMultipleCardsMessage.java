package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record ChooseMultipleCardsMessage(MessageType type, List<UUID> cardIds, List<CardView> cards, int maxCount, String prompt) {

    public ChooseMultipleCardsMessage(List<UUID> cardIds, List<CardView> cards, int maxCount, String prompt) {
        this(MessageType.CHOOSE_MULTIPLE_CARDS, cardIds, cards, maxCount, prompt);
    }
}
