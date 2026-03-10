package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record ScryMessage(MessageType type, List<CardView> cards, String prompt) {

    public ScryMessage(List<CardView> cards, String prompt) {
        this(MessageType.SCRY, cards, prompt);
    }
}
