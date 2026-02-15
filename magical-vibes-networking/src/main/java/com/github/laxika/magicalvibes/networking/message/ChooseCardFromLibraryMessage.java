package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseCardFromLibraryMessage(MessageType type, List<CardView> cards, String prompt) {

    public ChooseCardFromLibraryMessage(List<CardView> cards, String prompt) {
        this(MessageType.CHOOSE_CARD_FROM_LIBRARY, cards, prompt);
    }
}
