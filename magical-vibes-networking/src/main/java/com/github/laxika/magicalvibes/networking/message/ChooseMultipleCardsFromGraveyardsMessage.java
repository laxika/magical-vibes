package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record ChooseMultipleCardsFromGraveyardsMessage(MessageType type, List<UUID> cardIds, List<CardView> cards, int maxCount, String prompt) {

    public ChooseMultipleCardsFromGraveyardsMessage(List<UUID> cardIds, List<CardView> cards, int maxCount, String prompt) {
        this(MessageType.CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS, cardIds, cards, maxCount, prompt);
    }
}
