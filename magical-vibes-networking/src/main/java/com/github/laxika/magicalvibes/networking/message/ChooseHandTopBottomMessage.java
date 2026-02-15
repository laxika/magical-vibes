package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ChooseHandTopBottomMessage(MessageType type, List<CardView> cards, String prompt) {

    public ChooseHandTopBottomMessage(List<CardView> cards, String prompt) {
        this(MessageType.CHOOSE_HAND_TOP_BOTTOM, cards, prompt);
    }
}
