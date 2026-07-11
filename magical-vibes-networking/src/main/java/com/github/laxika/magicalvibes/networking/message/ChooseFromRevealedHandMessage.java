package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseFromRevealedHandMessage(MessageType type, List<CardView> cards, List<Integer> validIndices,
                                            String prompt, boolean optional) {

    public ChooseFromRevealedHandMessage(List<CardView> cards, List<Integer> validIndices, String prompt, boolean optional) {
        this(MessageType.CHOOSE_FROM_REVEALED_HAND, cards, validIndices, prompt, optional);
    }
}
