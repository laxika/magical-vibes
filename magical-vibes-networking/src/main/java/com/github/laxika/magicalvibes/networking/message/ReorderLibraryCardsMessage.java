package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ReorderLibraryCardsMessage(MessageType type, List<CardView> cards, String prompt) {

    public ReorderLibraryCardsMessage(List<CardView> cards, String prompt) {
        this(MessageType.REORDER_LIBRARY_CARDS, cards, prompt);
    }
}
