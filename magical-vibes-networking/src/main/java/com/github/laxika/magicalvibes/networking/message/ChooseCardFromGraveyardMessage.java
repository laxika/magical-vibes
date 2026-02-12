package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseCardFromGraveyardMessage(MessageType type, List<Integer> cardIndices, String prompt) {

    public ChooseCardFromGraveyardMessage(List<Integer> cardIndices, String prompt) {
        this(MessageType.CHOOSE_CARD_FROM_GRAVEYARD, cardIndices, prompt);
    }
}
