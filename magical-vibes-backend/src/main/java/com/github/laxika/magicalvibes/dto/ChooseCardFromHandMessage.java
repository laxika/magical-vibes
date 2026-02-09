package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseCardFromHandMessage(MessageType type, List<Integer> cardIndices, String prompt) {

    public ChooseCardFromHandMessage(List<Integer> cardIndices, String prompt) {
        this(MessageType.CHOOSE_CARD_FROM_HAND, cardIndices, prompt);
    }
}
